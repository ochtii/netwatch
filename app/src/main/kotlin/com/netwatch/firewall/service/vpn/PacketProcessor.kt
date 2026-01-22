package com.netwatch.firewall.service.vpn

import android.util.Log
import com.netwatch.firewall.domain.repository.AppRepository
import kotlinx.coroutines.runBlocking
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

/**
 * Handles packet parsing and processing logic
 */
class PacketProcessor(
    private val appRepository: AppRepository
) {
    companion object {
        private const val TAG = "PacketProcessor"
        
        // IP Protocol numbers
        private const val PROTOCOL_TCP = 6
        private const val PROTOCOL_UDP = 17
    }

    private val uidResolver = UidResolver()
    private val tunnelCache = mutableMapOf<String, DatagramChannel>()

    /**
     * Process a single IP packet
     */
    suspend fun processPacket(
        packet: ByteBuffer,
        vpnOutput: FileOutputStream,
        tunnel: DatagramChannel,
        protect: (DatagramSocket) -> Boolean
    ) {
        try {
            packet.position(0)

            // Parse IP header
            val ipVersion = (packet.get(0).toInt() shr 4) and 0xF
            if (ipVersion != 4) {
                // Only IPv4 supported for now
                return
            }

            val ipHeaderLength = (packet.get(0).toInt() and 0x0F) * 4
            val protocol = packet.get(9).toInt() and 0xFF
            
            // Extract source and destination IPs
            val sourceIp = extractIp(packet, 12)
            val destIp = extractIp(packet, 16)

            // Extract ports based on protocol
            var sourcePort = 0
            var destPort = 0

            when (protocol) {
                PROTOCOL_TCP, PROTOCOL_UDP -> {
                    sourcePort = packet.getShort(ipHeaderLength).toInt() and 0xFFFF
                    destPort = packet.getShort(ipHeaderLength + 2).toInt() and 0xFFFF
                }
                else -> {
                    // Unsupported protocol, allow by default
                    forwardPacket(packet, tunnel, destIp, destPort)
                    return
                }
            }

            // Resolve UID from source IP and port
            val uid = uidResolver.getUidForConnection(sourceIp, sourcePort, protocol)

            if (uid == -1) {
                // Could not resolve UID, allow by default for stability
                forwardPacket(packet, tunnel, destIp, destPort)
                return
            }

            // Check if UID is blocked
            val isBlocked = runBlocking {
                appRepository.isUidBlocked(uid)
            }

            if (isBlocked) {
                // Drop packet (do not forward)
                Log.d(TAG, "Blocked packet from UID $uid to $destIp:$destPort")
                return
            }

            // Track data usage
            val packetSize = packet.limit().toLong()
            runBlocking {
                appRepository.updateDataUsage(uid, packetSize, 0)
            }

            // Forward packet to actual network
            forwardPacket(packet, tunnel, destIp, destPort)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet", e)
        }
    }

    /**
     * Forward packet to the actual network destination
     */
    private fun forwardPacket(
        packet: ByteBuffer,
        tunnel: DatagramChannel,
        destIp: String,
        destPort: Int
    ) {
        try {
            packet.position(0)
            
            // For simplicity, we're using a basic forwarding mechanism
            // In production, you'd want more sophisticated NAT handling
            val destAddress = InetSocketAddress(destIp, destPort)
            tunnel.send(packet, destAddress)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding packet to $destIp:$destPort", e)
        }
    }

    /**
     * Extract IP address from packet buffer
     */
    private fun extractIp(buffer: ByteBuffer, offset: Int): String {
        return "${buffer.get(offset).toInt() and 0xFF}." +
                "${buffer.get(offset + 1).toInt() and 0xFF}." +
                "${buffer.get(offset + 2).toInt() and 0xFF}." +
                "${buffer.get(offset + 3).toInt() and 0xFF}"
    }
}
