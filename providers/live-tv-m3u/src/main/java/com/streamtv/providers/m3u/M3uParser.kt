package com.streamtv.providers.m3u

class M3uParser {

    companion object {
        private val TVG_ID_REGEX = Regex("""tvg-id=["']?([^"'\r\n]+)["']?""", RegexOption.IGNORE_CASE)
        private val TVG_NAME_REGEX = Regex("""tvg-name=["']?([^"'\r\n]+)["']?""", RegexOption.IGNORE_CASE)
        private val TVG_LOGO_REGEX = Regex("""tvg-logo=["']?([^"'\r\n]+)["']?""", RegexOption.IGNORE_CASE)
        private val GROUP_TITLE_REGEX = Regex("""group-title=["']?([^"'\r\n]+)["']?""", RegexOption.IGNORE_CASE)
        private val GLOBAL_EPG_REGEX = Regex("""(?:x-tvg-url|url-tvg)=["']?([^"'\s\r\n]+)["']?""", RegexOption.IGNORE_CASE)
    }

    data class ParseResult(
        val globalEpgUrl: String? = null,
        val entries: List<M3uEntry> = emptyList()
    )

    fun parse(content: String): ParseResult {
        val lines = content.lines()
        val entries = mutableListOf<M3uEntry>()
        var globalEpgUrl: String? = null

        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var currentTvgLogo: String? = null
        var currentGroupTitle: String? = null
        var currentName: String? = null
        val currentHeaders = mutableMapOf<String, String>()
        var hasActiveExtInf = false

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTM3U", ignoreCase = true)) {
                GLOBAL_EPG_REGEX.find(line)?.let {
                    globalEpgUrl = it.groupValues[1]
                }
                continue
            }

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                // If there was an unclosed previous extinf without a stream URL, reset
                currentHeaders.clear()

                currentTvgId = TVG_ID_REGEX.find(line)?.groupValues?.get(1)?.trim()
                currentTvgName = TVG_NAME_REGEX.find(line)?.groupValues?.get(1)?.trim()
                currentTvgLogo = TVG_LOGO_REGEX.find(line)?.groupValues?.get(1)?.trim()
                currentGroupTitle = GROUP_TITLE_REGEX.find(line)?.groupValues?.get(1)?.trim()

                // Extract Channel display name (everything after the last comma)
                val commaIndex = line.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                    line.substring(commaIndex + 1).trim()
                } else {
                    currentTvgName ?: currentTvgId ?: "Channel"
                }

                hasActiveExtInf = true
                continue
            }

            // Parse directive options
            if (line.startsWith("#EXTVLCOPT:", ignoreCase = true)) {
                val directive = line.removePrefix("#EXTVLCOPT:").removePrefix("#extvlcopt:").trim()
                val parts = directive.split('=', limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    if (key == "http-user-agent") {
                        currentHeaders["User-Agent"] = value
                    } else if (key == "http-referrer") {
                        currentHeaders["Referer"] = value
                    }
                }
                continue
            }

            // Ignore other comments
            if (line.startsWith("#")) {
                continue
            }

            // If line is not a comment, it is the stream URL
            if (hasActiveExtInf) {
                val channelName = currentName ?: "Channel ${entries.size + 1}"
                val safeId = generateChannelId(currentTvgId, channelName, line)

                entries.add(
                    M3uEntry(
                        id = safeId,
                        name = channelName,
                        streamUrl = line,
                        groupTitle = currentGroupTitle ?: "General",
                        tvgId = currentTvgId,
                        tvgName = currentTvgName,
                        tvgLogo = currentTvgLogo,
                        epgUrl = globalEpgUrl,
                        httpHeaders = currentHeaders.toMap()
                    )
                )

                // Reset per-channel state
                hasActiveExtInf = false
                currentName = null
                currentTvgId = null
                currentTvgName = null
                currentTvgLogo = null
                currentGroupTitle = null
                currentHeaders.clear()
            }
        }

        return ParseResult(globalEpgUrl = globalEpgUrl, entries = entries)
    }

    private fun generateChannelId(tvgId: String?, name: String, url: String): String {
        val base = tvgId?.takeIf { it.isNotBlank() } ?: name
        val sanitized = base.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(30)
        val hash = Math.abs(url.hashCode()).toString(16)
        return "m3u_${sanitized}_$hash"
    }
}
