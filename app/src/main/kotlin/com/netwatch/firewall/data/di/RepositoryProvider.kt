package com.netwatch.firewall.data.di

import android.content.Context
import com.netwatch.firewall.data.local.PreferencesDataSource
import com.netwatch.firewall.data.repository.AppRepositoryImpl
import com.netwatch.firewall.domain.repository.AppRepository

/**
 * Simple dependency injection provider for repositories
 * In a larger app, you'd use Hilt or Koin here
 */
object RepositoryProvider {
    
    @Volatile
    private var appRepository: AppRepository? = null
    
    fun provideAppRepository(context: Context): AppRepository {
        return appRepository ?: synchronized(this) {
            appRepository ?: createAppRepository(context).also {
                appRepository = it
            }
        }
    }
    
    private fun createAppRepository(context: Context): AppRepository {
        val preferencesDataSource = PreferencesDataSource(context.applicationContext)
        return AppRepositoryImpl(context.applicationContext, preferencesDataSource)
    }
}
