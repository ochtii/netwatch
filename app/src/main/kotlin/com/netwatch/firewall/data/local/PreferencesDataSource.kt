package com.netwatch.firewall.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-based data source for app preferences (blocked apps list)
 */
class PreferencesDataSource(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netwatch_preferences")
    
    companion object {
        private val BLOCKED_PACKAGES_KEY = stringSetPreferencesKey("blocked_packages")
    }
    
    /**
     * Get flow of blocked package names
     */
    fun getBlockedPackages(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[BLOCKED_PACKAGES_KEY] ?: emptySet()
        }
    }
    
    /**
     * Add a package to the blocked list
     */
    suspend fun blockPackage(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentBlocked = preferences[BLOCKED_PACKAGES_KEY] ?: emptySet()
            preferences[BLOCKED_PACKAGES_KEY] = currentBlocked + packageName
        }
    }
    
    /**
     * Remove a package from the blocked list
     */
    suspend fun unblockPackage(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentBlocked = preferences[BLOCKED_PACKAGES_KEY] ?: emptySet()
            preferences[BLOCKED_PACKAGES_KEY] = currentBlocked - packageName
        }
    }
    
    /**
     * Check if a package is blocked
     */
    suspend fun isPackageBlocked(packageName: String): Boolean {
        var isBlocked = false
        context.dataStore.data.collect { preferences ->
            val blockedPackages = preferences[BLOCKED_PACKAGES_KEY] ?: emptySet()
            isBlocked = packageName in blockedPackages
        }
        return isBlocked
    }
    
    /**
     * Clear all blocked packages
     */
    suspend fun clearBlockedPackages() {
        context.dataStore.edit { preferences ->
            preferences.remove(BLOCKED_PACKAGES_KEY)
        }
    }
}
