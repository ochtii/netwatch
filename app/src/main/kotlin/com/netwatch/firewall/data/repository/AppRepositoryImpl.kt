package com.netwatch.firewall.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.netwatch.firewall.data.local.PreferencesDataSource
import com.netwatch.firewall.domain.model.AppEntry
import com.netwatch.firewall.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of AppRepository
 * Manages app data from PackageManager and DataStore
 */
class AppRepositoryImpl(
    private val context: Context,
    private val preferencesDataSource: PreferencesDataSource
) : AppRepository {
    
    private val packageManager: PackageManager = context.packageManager
    
    // In-memory storage for current session data usage
    // Key: UID, Value: Pair(sent, received)
    private val dataUsageMap = ConcurrentHashMap<Int, Pair<Long, Long>>()
    
    override fun getInstalledApps(): Flow<List<AppEntry>> {
        return try {
            combine(
                getInstalledAppsFlow(),
                preferencesDataSource.getBlockedPackages()
            ) { apps, blockedPackages ->
                apps.map { app ->
                    app.copy(
                        isBlocked = app.packageName in blockedPackages,
                        dataSent = dataUsageMap[app.uid]?.first ?: 0L,
                        dataReceived = dataUsageMap[app.uid]?.second ?: 0L
                    )
                }.sortedBy { it.label.lowercase() }
            }
        } catch (e: Exception) {
            // Return empty flow on error
            flow { emit(emptyList<AppEntry>()) }
        }
    }
    
    private fun getInstalledAppsFlow(): Flow<List<AppEntry>> = flow {
        try {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { appInfo ->
                    // Filter to only user-installed apps (exclude system apps)
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
                .map { appInfo ->
                    AppEntry(
                        packageName = appInfo.packageName,
                        uid = appInfo.uid,
                        label = packageManager.getApplicationLabel(appInfo).toString(),
                        icon = packageManager.getApplicationIcon(appInfo),
                        dataSent = 0L,
                        dataReceived = 0L,
                        isBlocked = false
                    )
                }
            emit(apps)
        } catch (e: Exception) {
            // Fallback to empty list if permission denied
            emit(emptyList())
        }
    }
    
    override fun getBlockedPackages(): Flow<Set<String>> {
        return preferencesDataSource.getBlockedPackages()
    }
    
    override suspend fun setAppBlocked(packageName: String, isBlocked: Boolean) {
        if (isBlocked) {
            preferencesDataSource.blockPackage(packageName)
        } else {
            preferencesDataSource.unblockPackage(packageName)
        }
    }
    
    override suspend fun isPackageBlocked(packageName: String): Boolean {
        return preferencesDataSource.isPackageBlocked(packageName)
    }
    
    override suspend fun isUidBlocked(uid: Int): Boolean {
        // Find package name for UID and check if it's blocked
        val packages = packageManager.getPackagesForUid(uid) ?: return false
        return packages.any { packageName ->
            preferencesDataSource.isPackageBlocked(packageName)
        }
    }
    
    override suspend fun updateDataUsage(uid: Int, sentBytes: Long, receivedBytes: Long) {
        dataUsageMap.compute(uid) { _, current ->
            val currentSent = current?.first ?: 0L
            val currentReceived = current?.second ?: 0L
            Pair(currentSent + sentBytes, currentReceived + receivedBytes)
        }
    }
    
    override suspend fun clearDataUsage() {
        dataUsageMap.clear()
    }
}
