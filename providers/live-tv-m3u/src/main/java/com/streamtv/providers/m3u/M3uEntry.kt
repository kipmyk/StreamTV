package com.streamtv.providers.m3u

data class M3uEntry(
    val id: String,
    val name: String,
    val streamUrl: String,
    val groupTitle: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val epgUrl: String? = null,
    val httpHeaders: Map<String, String> = emptyMap()
)
