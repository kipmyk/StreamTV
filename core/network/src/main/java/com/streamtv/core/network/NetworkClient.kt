package com.streamtv.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NetworkClient {

    suspend fun get(urlString: String, headers: Map<String, String> = emptyMap()): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Support local file import (e.g. file:///path/to/playlist.m3u or /path/to/playlist.m3u)
                if (urlString.startsWith("file://") || urlString.startsWith("/")) {
                    val filePath = if (urlString.startsWith("file://")) urlString.removePrefix("file://") else urlString
                    val file = File(filePath)
                    if (file.exists()) {
                        return@withContext Result.success(file.readText())
                    } else {
                        return@withContext Result.failure(IllegalArgumentException("Local file not found: $filePath"))
                    }
                }

                val url = URL(urlString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("User-Agent", "StreamTV/1.0 (Android TV)")
                    headers.forEach { (key, value) -> setRequestProperty(key, value) }
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.use { it.readText() }
                    Result.success(response)
                } else {
                    val errorBody = try {
                        val errStream = connection.errorStream
                        if (errStream != null) BufferedReader(InputStreamReader(errStream)).use { it.readText() } else ""
                    } catch (_: Exception) { "" }
                    Result.failure(Exception("HTTP $responseCode: ${connection.responseMessage}. $errorBody".trim()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
