package com.netwatch.firewall

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netwatch.firewall.presentation.applist.AppListScreen
import com.netwatch.firewall.presentation.applist.AppListViewModel
import com.netwatch.firewall.presentation.applist.AppListViewModelFactory
import com.netwatch.firewall.service.vpn.NetWatchVpnService
import com.netwatch.firewall.ui.theme.NetWatchTheme

class MainActivity : ComponentActivity() {
    
    lateinit var viewModel: AppListViewModel
    
    // VPN permission launcher
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, start VPN
            startVpnService()
        } else {
            // Permission denied
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
            viewModel.toggleVpnService(false)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetWatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NetWatchApp(
                        onToggleVpn = { start -> handleVpnToggle(start) }
                    )
                }
            }
        }
    }
    
    private fun handleVpnToggle(start: Boolean) {
        if (start) {
            // Request VPN permission
            val intent = VpnService.prepare(this)
            if (intent != null) {
                // Need to request permission
                vpnPermissionLauncher.launch(intent)
            } else {
                // Already have permission
                startVpnService()
            }
        } else {
            stopVpnService()
        }
        viewModel.toggleVpnService(start)
    }
    
    private fun startVpnService() {
        val intent = Intent(this, NetWatchVpnService::class.java).apply {
            action = NetWatchVpnService.ACTION_START
        }
        startService(intent)
        Toast.makeText(this, "VPN started", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopVpnService() {
        val intent = Intent(this, NetWatchVpnService::class.java).apply {
            action = NetWatchVpnService.ACTION_STOP
        }
        startService(intent)
        Toast.makeText(this, "VPN stopped", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun NetWatchApp(
    onToggleVpn: (Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as MainActivity
    
    val viewModel: AppListViewModel = viewModel(
        factory = AppListViewModelFactory.create(context)
    )
    
    // Store viewModel reference in activity for VPN callbacks
    activity.viewModel = viewModel
    
    val uiState by viewModel.uiState.collectAsState()
    val isVpnActive by viewModel.isVpnActive.collectAsState()

    AppListScreen(
        uiState = uiState,
        isVpnActive = isVpnActive,
        onToggleVpn = onToggleVpn,
        onToggleAppBlocked = { packageName, isBlocked ->
            viewModel.toggleAppBlocked(packageName, isBlocked)
        },
        onRefresh = { viewModel.refresh() }
    )
}
