package com.streamtv.providers.m3u

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.MediaItem
import com.streamtv.core.model.StreamResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    private val parser = M3uParser()

    @Test
    fun testParseStandardPlaylistWithAllAttributes() {
        val sampleM3u = """
            #EXTM3U x-tvg-url="https://epg.example.com/guide.xml"
            
            #EXTINF:-1 tvg-id="bbc1.uk" tvg-name="BBC One" tvg-logo="https://logos.example.com/bbc1.png" group-title="News",BBC One HD
            #EXTVLCOPT:http-user-agent=CustomStreamClient/1.0
            https://stream.example.com/bbc1/live.m3u8
            
            #EXTINF:-1 tvg-id="espn.us" tvg-name="ESPN HD" tvg-logo="https://logos.example.com/espn.png" group-title="Sports",ESPN Sports
            https://stream.example.com/espn/live.m3u8
        """.trimIndent()

        val result = parser.parse(sampleM3u)

        assertEquals("https://epg.example.com/guide.xml", result.globalEpgUrl)
        assertEquals(2, result.entries.size)

        val channel1 = result.entries[0]
        assertEquals("BBC One HD", channel1.name)
        assertEquals("https://stream.example.com/bbc1/live.m3u8", channel1.streamUrl)
        assertEquals("News", channel1.groupTitle)
        assertEquals("bbc1.uk", channel1.tvgId)
        assertEquals("https://logos.example.com/bbc1.png", channel1.tvgLogo)
        assertEquals("CustomStreamClient/1.0", channel1.httpHeaders["User-Agent"])

        val channel2 = result.entries[1]
        assertEquals("ESPN Sports", channel2.name)
        assertEquals("Sports", channel2.groupTitle)
        assertEquals("https://stream.example.com/espn/live.m3u8", channel2.streamUrl)
    }

    @Test
    fun testParseMinimalPlaylistWithoutOptionalAttributes() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1,Sky Cinema
            https://stream.example.com/skycinema.m3u8
        """.trimIndent()

        val result = parser.parse(sampleM3u)
        assertEquals(1, result.entries.size)
        val channel = result.entries[0]
        assertEquals("Sky Cinema", channel.name)
        assertEquals("General", channel.groupTitle)
        assertEquals("https://stream.example.com/skycinema.m3u8", channel.streamUrl)
    }

    @Test
    fun testParseEmptyContentReturnsZeroEntries() {
        val result = parser.parse("")
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun testParseMalformedEntriesGracefully() {
        val sampleM3u = """
            #EXTM3U
            # Some random comment
            #EXTINF:-1 tvg-name="Orphan Channel"
            # Missing URL here
            #EXTINF:-1 tvg-id="valid.1",Valid Channel
            https://stream.example.com/valid.m3u8
        """.trimIndent()

        val result = parser.parse(sampleM3u)
        // Orphan Channel without a URL is discarded, valid channel parsed
        assertEquals(1, result.entries.size)
        assertEquals("Valid Channel", result.entries[0].name)
    }

    @Test
    fun testM3uStreamProviderResolveStreamSuccessAndError() = runBlocking {
        val provider = M3uStreamProvider(parser = parser)

        // Attempting to resolve stream without loaded playlist returns Error
        val dummyItem = MediaItem(
            id = "non_existent_channel",
            title = "Unknown Channel",
            contentType = ContentType.LIVE_TV
        )
        val errorResult = provider.resolveStream(dummyItem)
        assertTrue(errorResult is StreamResult.Error)
        val errorMessage = (errorResult as StreamResult.Error).message
        assertTrue(errorMessage.contains("not found"))

        // Direct stream URL resolves directly
        val directItem = MediaItem(
            id = "direct_channel",
            title = "Direct Channel",
            contentType = ContentType.LIVE_TV,
            directStreamUrl = "https://stream.example.com/direct.m3u8"
        )
        val successResult = provider.resolveStream(directItem)
        assertTrue(successResult is StreamResult.Success)
        assertEquals("https://stream.example.com/direct.m3u8", (successResult as StreamResult.Success).url)
    }
}
