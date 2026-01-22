package com.netwatch.firewall.domain.model

import android.graphics.drawable.Drawable

/**
 * Domain model representing an installed application with its network usage stats
 */
data class AppEntry(
    val packageName: String,
    val uid: Int,
    val label: String,
    val icon: Drawable?,
    val dataSent: Long = 0L,      // Bytes sent in current session
    val dataReceived: Long = 0L,  // Bytes received in current session
    val isBlocked: Boolean = false
) {
    /**
     * Total data usage in bytes (sent + received)
     */
    val totalDataUsage: Long
        get() = dataSent + dataReceived

    /**
     * Format bytes to human-readable string (e.g., "5.2 MB")
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    /**
     * Get formatted sent data string
     */
    val formattedSent: String
        get() = formatBytes(dataSent)

    /**
     * Get formatted received data string
     */
    val formattedReceived: String
        get() = formatBytes(dataReceived)
}
