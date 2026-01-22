package com.netwatch.firewall.service.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.netwatch.firewall.MainActivity
import com.netwatch.firewall.R
import com.netwatch.firewall.data.di.RepositoryProvider
import com.netwatch.firewall.domain.repository.AppRepository
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

/**
 * VPN Service for intercepting and controlling network traffic
 * Implements no-root firewall functionality
 */
class NetWatchVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceScope: CoroutineScope? = null
    private lateinit var appRepository: AppRepository
    private lateinit var packetProcessor: PacketProcessor
    private var isRunning = false

    companion object {
        private const val TAG = "NetWatchVpnService"
        private const val VPN_MTU = 1500
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val NOTIFICATION_CHANNEL_ID = "netwatch_vpn_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_START = "com.netwatch.firewall.START_VPN"
        const val ACTION_STOP = "com.netwatch.firewall.STOP_VPN"
    }

    override fun onCreate() {
        super.onCreate()
        appRepository = RepositoryProvider.provideAppRepository(this)
        packetProcessor = PacketProcessor(appRepository)
        createNotificationChannel()
        Log.d(TAG, "VPN Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START -> {
                startVpn()
                START_STICKY
            }
            ACTION_STOP -> {
                stopVpn()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    /**
     * Start the VPN service
     */
    private fun startVpn() {
        if (isRunning) {
            Log.d(TAG, "VPN already running")
            return
        }

        try {
            // Build VPN interface
            val builder = Builder()
                .setSession("NetWatch")
                .addAddress(VPN_ADDRESS, 32)
                .addRoute(VPN_ROUTE, 0)
                .setMtu(VPN_MTU)
                .setBlocking(true)

            // Add DNS servers
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("8.8.4.4")

            // Establish VPN
            vpnInterface = builder.establish()

            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                stopSelf()
                return
            }

            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification())

            // Start packet processing in coroutine
            serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            serviceScope?.launch {
                processPackets()
            }

            Log.d(TAG, "VPN started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopVpn()
        }
    }

    /**
     * Stop the VPN service
     */
    private fun stopVpn() {
        isRunning = false

        serviceScope?.cancel()
        serviceScope = null

        vpnInterface?.close()
        vpnInterface = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "VPN stopped")
    }

    /**
     * Main packet processing loop
     */
    private suspend fun processPackets() {
        val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
        val packet = ByteBuffer.allocate(VPN_MTU)

        // Create tunnel for outgoing packets
        val tunnel = DatagramChannel.open()
        tunnel.configureBlocking(false)
        protect(tunnel.socket())

        try {
            while (isRunning && !Thread.currentThread().isInterrupted) {
                // Read packet from VPN interface
                packet.clear()
                val length = vpnInput.read(packet.array())

                if (length > 0) {
                    packet.limit(length)
                    
                    // Process the packet
                    withContext(Dispatchers.Default) {
                        packetProcessor.processPacket(
                            packet = packet,
                            vpnOutput = vpnOutput,
                            tunnel = tunnel,
                            protect = { socket -> protect(socket) }
                        )
                    }
                }

                // Small delay to prevent CPU spinning
                delay(1)
            }
        } catch (e: Exception) {
            if (isRunning) {
                Log.e(TAG, "Error processing packets", e)
            }
        } finally {
            tunnel.close()
        }
    }

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "NetWatch VPN is active"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create foreground service notification
     */
    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("NetWatch Active")
            .setContentText("Monitoring and controlling network traffic")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        Log.d(TAG, "VPN Service destroyed")
    }

    override fun onRevoke() {
        super.onRevoke()
        stopVpn()
        Log.d(TAG, "VPN permission revoked")
    }
}
