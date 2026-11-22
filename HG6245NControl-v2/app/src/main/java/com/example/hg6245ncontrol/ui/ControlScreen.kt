package com.example.hg6245ncontrol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hg6245ncontrol.model.RouterUiState

private enum class ControlTab(val label: String) {
    DASHBOARD("Dashboard"),
    NETWORK("Wi-Fi & Sec"),
    DEVICES("Devices"),
    SETTINGS("Settings")
}

@Composable
fun ControlAppScreen(
    uiState: RouterUiState,
    onRefresh: () -> Unit,
    onUpdateHost: (String) -> Unit,
    onUpdatePollInterval: (Int) -> Unit,
    onToggleAutoRefresh: (Boolean) -> Unit,
    onApplyWifi: (ssid: String, passphrase: String, bandSteering: Boolean) -> Unit,
    onApplyFirewall: (level: String, ipsec: Boolean, pptp: Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(ControlTab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ControlTab.entries.forEach { tab ->
                    val icon = when (tab) {
                        ControlTab.DASHBOARD -> Icons.Default.Dashboard
                        ControlTab.NETWORK -> Icons.Default.Wifi
                        ControlTab.DEVICES -> Icons.Default.Devices
                        ControlTab.SETTINGS -> Icons.Default.Settings
                    }
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ControlTab.DASHBOARD -> DashboardTab(uiState = uiState, onRefresh = onRefresh)
                ControlTab.NETWORK -> NetworkSecurityTab(
                    uiState = uiState,
                    onApplyWifi = onApplyWifi,
                    onApplyFirewall = onApplyFirewall
                )
                ControlTab.DEVICES -> DevicesTab(uiState = uiState)
                ControlTab.SETTINGS -> SettingsTab(
                    uiState = uiState,
                    onUpdateHost = onUpdateHost,
                    onUpdatePollInterval = onUpdatePollInterval,
                    onToggleAutoRefresh = onToggleAutoRefresh,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
private fun DashboardTab(
    uiState: RouterUiState,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${uiState.deviceModel} Control",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalIconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Resource & Status", style = MaterialTheme.typography.titleMedium)

                    val cpuRatio = (uiState.cpuUsagePercent / 100f).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU Usage")
                        Text("${"%.0f".format(uiState.cpuUsagePercent)}%", fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(
                        progress = { cpuRatio },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val memRatio = (uiState.memoryUsagePercent / 100f).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Memory Usage")
                        Text("${"%.2f".format(uiState.memoryUsagePercent)}%", fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(
                        progress = { memRatio },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()
                    Text("System UpTime: ${uiState.uptimeText}")
                    Text("Software Version: ${uiState.softwareVersion}")
                    Text("MAC Address: ${uiState.macAddress}")
                    Text("SSID Aktif: ${uiState.ssidName} (${uiState.securityMode})")
                    Text("RSSI: 2.4G ${uiState.wifiRssi24G} dBm | 5G ${uiState.wifiRssi5G} dBm")
                }
            }
        }
    }
}

@Composable
private fun NetworkSecurityTab(
    uiState: RouterUiState,
    onApplyWifi: (String, String, Boolean) -> Unit,
    onApplyFirewall: (String, Boolean, Boolean) -> Unit
) {
    var ssidInput by remember(uiState.ssidName) { mutableStateOf(uiState.ssidName) }
    var passphraseInput by remember { mutableStateOf("") }
    var bandSteering by remember(uiState.bandSteeringEnabled) { mutableStateOf(uiState.bandSteeringEnabled) }

    var firewallLevel by remember(uiState.firewallLevel) { mutableStateOf(uiState.firewallLevel) }
    var ipsec by remember(uiState.ipsecPassthrough) { mutableStateOf(uiState.ipsecPassthrough) }
    var pptp by remember(uiState.pptpPassthrough) { mutableStateOf(uiState.pptpPassthrough) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("WLAN Settings", style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Band Steering")
                        Switch(checked = bandSteering, onCheckedChange = { bandSteering = it })
                    }

                    OutlinedTextField(
                        value = ssidInput,
                        onValueChange = { ssidInput = it },
                        label = { Text("SSID Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passphraseInput,
                        onValueChange = { passphraseInput = it },
                        label = { Text("Passphrase (8-63 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { onApplyWifi(ssidInput, passphraseInput, bandSteering) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Wi-Fi")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Firewall & VPN Passthrough", style = MaterialTheme.typography.titleMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            FilterChip(
                                selected = firewallLevel == level,
                                onClick = { firewallLevel = level },
                                label = { Text(level) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("IPSec Passthrough")
                        Switch(checked = ipsec, onCheckedChange = { ipsec = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PPTP Passthrough")
                        Switch(checked = pptp, onCheckedChange = { pptp = it })
                    }

                    Button(
                        onClick = { onApplyFirewall(firewallLevel, ipsec, pptp) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Security")
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesTab(uiState: RouterUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Perangkat Terhubung (${uiState.connectedDevices.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(uiState.connectedDevices) { client ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(client.hostname, fontWeight = FontWeight.SemiBold)
                    Text("IP: ${client.ipAddress} | ${client.bandOrPort}", style = MaterialTheme.typography.bodySmall)
                    Text("MAC: ${client.macAddress}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    uiState: RouterUiState,
    onUpdateHost: (String) -> Unit,
    onUpdatePollInterval: (Int) -> Unit,
    onToggleAutoRefresh: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    var hostInput by remember(uiState.routerHost) { mutableStateOf(uiState.routerHost) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Koneksi Perangkat", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Router / ONT URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onUpdateHost(hostInput)
                            onRefresh()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan & Tes Koneksi")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Refresh")
                        Switch(
                            checked = uiState.autoRefresh,
                            onCheckedChange = onToggleAutoRefresh
                        )
                    }

                    Text("Interval: ${uiState.pollIntervalSec} detik")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 10).forEach { sec ->
                            FilterChip(
                                selected = uiState.pollIntervalSec == sec,
                                onClick = { onUpdatePollInterval(sec) },
                                label = { Text("${sec}s") }
                            )
                        }
                    }
                }
            }
        }
    }
}
