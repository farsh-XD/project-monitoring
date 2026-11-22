package com.example.hg6245ncontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.hg6245ncontrol.data.RouterRepository
import com.example.hg6245ncontrol.model.RouterUiState
import com.example.hg6245ncontrol.ui.ControlAppScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = RouterRepository()

        setContent {
            MaterialTheme {
                var uiState by remember { mutableStateOf(RouterUiState()) }
                val scope = rememberCoroutineScope()

                fun refreshNow() {
                    scope.launch {
                        uiState = uiState.copy(isLoading = true)
                        uiState = repository.fetchStatusFromRouter(uiState)
                    }
                }

                LaunchedEffect(uiState.autoRefresh, uiState.pollIntervalSec, uiState.routerHost) {
                    refreshNow()
                    while (uiState.autoRefresh) {
                        delay(uiState.pollIntervalSec * 1000L)
                        uiState = repository.fetchStatusFromRouter(uiState)
                    }
                }

                ControlAppScreen(
                    uiState = uiState,
                    onRefresh = { refreshNow() },
                    onUpdateHost = { newHost ->
                        uiState = uiState.copy(routerHost = newHost.trim())
                    },
                    onUpdatePollInterval = { sec ->
                        uiState = uiState.copy(pollIntervalSec = sec)
                    },
                    onToggleAutoRefresh = { enabled ->
                        uiState = uiState.copy(autoRefresh = enabled)
                    },
                    onApplyWifi = { ssid, passphrase, bandSteering ->
                        val passInfo = if (passphrase.isNotBlank()) " & Passphrase diperbarui" else ""
                        uiState = uiState.copy(
                            ssidName = ssid.ifBlank { uiState.ssidName },
                            bandSteeringEnabled = bandSteering,
                            lastMessage = "Konfigurasi Wi-Fi ($ssid) disimpan di state lokal$passInfo"
                        )
                    },
                    onApplyFirewall = { level, ipsec, pptp ->
                        uiState = uiState.copy(
                            firewallLevel = level,
                            ipsecPassthrough = ipsec,
                            pptpPassthrough = pptp,
                            lastMessage = "Firewall ($level) & VPN Passthrough diperbarui di state lokal"
                        )
                    }
                )
            }
        }
    }
}
