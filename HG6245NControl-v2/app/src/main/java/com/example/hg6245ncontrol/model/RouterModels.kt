package com.example.hg6245ncontrol.model

data class ConnectedClient(
    val hostname: String,
    val ipAddress: String,
    val macAddress: String,
    val bandOrPort: String
)

data class RouterUiState(
    val routerHost: String = "http://192.168.1.100",
    val deviceModel: String = "HG6245N",
    val softwareVersion: String = "RP2870",
    val macAddress: String = "60:A4:D1:81:4C:A8",
    val cpuUsagePercent: Float = 25f,
    val memoryUsagePercent: Float = 45.87f,
    val uptimeText: String = "11 d 19 h 26 m 10 s",
    val ssidName: String = "Family",
    val securityMode: String = "WPA2-PSK",
    val wifiRssi24G: String = "-40",
    val wifiRssi5G: String = "-20",
    val bandSteeringEnabled: Boolean = true,
    val firewallLevel: String = "Low",
    val ipsecPassthrough: Boolean = true,
    val pptpPassthrough: Boolean = true,
    val pollIntervalSec: Int = 5,
    val autoRefresh: Boolean = true,
    val isLoading: Boolean = false,
    val isReachable: Boolean = true,
    val lastMessage: String = "Android 10 Ready • Target 192.168.1.100",
    val connectedDevices: List<ConnectedClient> = listOf(
        ConnectedClient("Android-10-Client", "192.168.1.105", "AA:BB:CC:11:22:33", "5G Wi-Fi"),
        ConnectedClient("LAN-PC", "192.168.1.102", "AA:BB:CC:44:55:66", "LAN 1")
    )
)
