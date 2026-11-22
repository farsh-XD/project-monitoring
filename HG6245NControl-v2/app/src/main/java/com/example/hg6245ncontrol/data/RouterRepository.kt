package com.example.hg6245ncontrol.data

import com.example.hg6245ncontrol.model.RouterUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class RouterRepository {

    suspend fun fetchStatusFromRouter(current: RouterUiState): RouterUiState =
        withContext(Dispatchers.IO) {
            val baseUrl = current.routerHost.trim().trimEnd('/')
            val candidateUrls = listOf(
                "$baseUrl/html/status/deviceinfo.asp",
                "$baseUrl/html/status/status.asp",
                "$baseUrl/"
            )

            for (target in candidateUrls) {
                try {
                    val conn = (URL(target).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 2500
                        readTimeout = 2500
                    }
                    val code = conn.responseCode
                    val body = if (code in 200..299) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        ""
                    }
                    conn.disconnect()

                    if (body.isNotBlank()) {
                        val cpuMatch = Regex("""CPU\s*Usage[^0-9]*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
                            .find(body)?.groupValues?.getOrNull(1)?.toFloatOrNull()
                        val memMatch = Regex("""Memory\s*Usage[^0-9]*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
                            .find(body)?.groupValues?.getOrNull(1)?.toFloatOrNull()
                        val uptimeMatch = Regex("""System\s*UpTime[^<]*<[^>]*>([^<]+)""", RegexOption.IGNORE_CASE)
                            .find(body)?.groupValues?.getOrNull(1)?.trim()

                        return@withContext current.copy(
                            cpuUsagePercent = cpuMatch ?: current.cpuUsagePercent,
                            memoryUsagePercent = memMatch ?: current.memoryUsagePercent,
                            uptimeText = uptimeMatch ?: current.uptimeText,
                            isReachable = true,
                            isLoading = false,
                            lastMessage = "Terhubung ke $baseUrl (HTTP $code)"
                        )
                    }
                } catch (_: Exception) {
                    // Coba kandidat URL berikutnya
                }
            }

            current.copy(
                isReachable = false,
                isLoading = false,
                lastMessage = "Mode tampilan lokal/cached ($baseUrl belum merespons langsung)"
            )
        }
}
