package com.streamtv.providers.m3u

import com.streamtv.core.model.ContentType
import com.streamtv.core.model.StreamResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeLiveTvApiTest {

    @Test
    fun testFeedsContainNewsAndSports() {
        val feeds = FreeLiveTvApi.FEEDS
        assertTrue("Feeds should not be empty", feeds.isNotEmpty())

        val newsFeed = feeds.find { it.category == "News" }
        assertNotNull("Should contain News feed", newsFeed)
        assertTrue("News URL should point to m3u", newsFeed!!.m3uUrl.endsWith(".m3u"))

        val sportsFeed = feeds.find { it.category == "Sports" }
        assertNotNull("Should contain Sports feed", sportsFeed)
        assertEquals(ContentType.SPORTS, sportsFeed!!.defaultContentType)
    }

    @Test
    fun testVerifiedDefaultChannelsArePlayable() {
        val defaultChannels = FreeLiveTvApi.VERIFIED_DEFAULT_CHANNELS
        assertTrue("Should have at least 4 default verified channels", defaultChannels.size >= 4)

        defaultChannels.forEach { item ->
            assertNotNull("Channel title cannot be null", item.title)
            assertNotNull("Channel stream URL cannot be null", item.directStreamUrl)
            assertTrue("Stream URL must be an HLS m3u8 stream", item.directStreamUrl!!.endsWith(".m3u8"))
        }
    }

    @Test
    fun testM3uProviderAutoInitializesWithVerifiedChannels() = runBlocking {
        val provider = M3uStreamProvider()
        assertTrue("Provider should be loaded by default with verified channels", provider.isLoaded)
        assertTrue("Total channels should be >= 4", provider.totalChannels >= 4)

        val trendingResult = provider.getTrending(ContentType.LIVE_TV)
        assertTrue(trendingResult.isSuccess)
        val channels = trendingResult.getOrNull()
        assertNotNull(channels)
        assertTrue(channels!!.isNotEmpty())

        // Test resolving stream for first verified channel
        val firstChannel = channels.first()
        val streamResult = provider.resolveStream(firstChannel)
        assertTrue("Stream resolution must be Success", streamResult is StreamResult.Success)
        val success = streamResult as StreamResult.Success
        assertTrue(success.url.startsWith("http"))
    }
}
