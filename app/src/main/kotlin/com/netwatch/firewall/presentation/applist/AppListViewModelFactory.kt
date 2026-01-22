package com.netwatch.firewall.presentation.applist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.netwatch.firewall.data.di.RepositoryProvider
import com.netwatch.firewall.domain.repository.AppRepository

/**
 * Factory for creating AppListViewModel with dependencies
 */
class AppListViewModelFactory(
    private val appRepository: AppRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {
            return AppListViewModel(appRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        fun create(context: Context): AppListViewModelFactory {
            return AppListViewModelFactory(
                appRepository = RepositoryProvider.provideAppRepository(context)
            )
        }
    }
}
