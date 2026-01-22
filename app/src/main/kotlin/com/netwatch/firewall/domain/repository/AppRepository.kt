package com.netwatch.firewall.domain.repository

import com.netwatch.firewall.domain.model.AppEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing installed applications and their blocking status
 */
interface AppRepository {
    
    /**
     * Get a flow of all user-installed applications
     * Excludes system apps by default
     */
    fun getInstalledApps(): Flow<List<AppEntry>>
    
    /**
     * Get the set of blocked package names
     */
    fun getBlockedPackages(): Flow<Set<String>>
    
    /**
     * Block or unblock an app by package name
     * @param packageName The package name of the app
     * @param isBlocked True to block, false to allow
     */
    suspend fun setAppBlocked(packageName: String, isBlocked: Boolean)
    
    /**
     * Check if a specific package is blocked
     * @param packageName The package name to check
     */
    suspend fun isPackageBlocked(packageName: String): Boolean
    
    /**
     * Check if a specific UID is blocked
     * @param uid The application UID
     */
    suspend fun isUidBlocked(uid: Int): Boolean
    
    /**
     * Update data usage for a specific UID
     * @param uid The application UID
     * @param sentBytes Bytes sent
     * @param receivedBytes Bytes received
     */
    suspend fun updateDataUsage(uid: Int, sentBytes: Long, receivedBytes: Long)
    
    /**
     * Clear all data usage statistics (reset session)
     */
    suspend fun clearDataUsage()
}
