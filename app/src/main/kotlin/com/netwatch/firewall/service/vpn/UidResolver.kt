package com.netwatch.firewall.service.vpn

import android.util.Log
import java.io.File

/**
 * Resolves UIDs from network connections by reading /proc/net/tcp and /proc/net/udp
 * This is a basic implementation for UID tracking without root access
 */
class UidResolver {
    
    companion object {
        private const val TAG = "UidResolver"
        private const val PROC_NET_TCP = "/proc/net/tcp"
        private const val PROC_NET_TCP6 = "/proc/net/tcp6"
        private const val PROC_NET_UDP = "/proc/net/udp"
        private const val PROC_NET_UDP6 = "/proc/net/udp6"
        
        private const val PROTOCOL_TCP = 6
        private const val PROTOCOL_UDP = 17
    }

    // Cache for UID lookups (port -> UID mapping)
    private val uidCache = mutableMapOf<String, Int>()
    private var lastCacheUpdate = 0L
    private val cacheValidityMs = 5000L // 5 seconds

    /**
     * Get UID for a connection based on source IP, port, and protocol
     */
    fun getUidForConnection(sourceIp: String, sourcePort: Int, protocol: Int): Int {
        val now = System.currentTimeMillis()
        
        // Refresh cache if expired
        if (now - lastCacheUpdate > cacheValidityMs) {
            refreshUidCache()
            lastCacheUpdate = now
        }

        // Create cache key
        val key = "${protocol}:${sourcePort}"
        
        return uidCache[key] ?: -1
    }

    /**
     * Refresh the UID cache by reading /proc/net files
     */
    private fun refreshUidCache() {
        uidCache.clear()

        try {
            // Read TCP connections
            parseProcNet(PROC_NET_TCP, PROTOCOL_TCP)
            
            // Read UDP connections
            parseProcNet(PROC_NET_UDP, PROTOCOL_UDP)
            
            Log.d(TAG, "UID cache refreshed with ${uidCache.size} entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing UID cache", e)
        }
    }

    /**
     * Parse /proc/net/tcp or /proc/net/udp file
     */
    private fun parseProcNet(filename: String, protocol: Int) {
        val file = File(filename)
        if (!file.exists()) {
            return
        }

        try {
            file.readLines().drop(1).forEach { line ->
                // Example line format:
                // sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
                // 0: 0100007F:1F90 00000000:0000 0A 00000000:00000000 00:00000000 00000000  1000        0 12345
                
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size < 8) {
                    return@forEach
                }

                try {
                    // Extract local address and port
                    val localAddress = parts[1]
                    val addressParts = localAddress.split(":")
                    if (addressParts.size != 2) {
                        return@forEach
                    }

                    // Port is in hex
                    val port = addressParts[1].toInt(16)
                    
                    // UID is typically at index 7
                    val uid = parts[7].toInt()

                    // Store in cache
                    val key = "${protocol}:${port}"
                    uidCache[key] = uid

                } catch (e: Exception) {
                    // Skip malformed lines
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing $filename", e)
        }
    }
}
