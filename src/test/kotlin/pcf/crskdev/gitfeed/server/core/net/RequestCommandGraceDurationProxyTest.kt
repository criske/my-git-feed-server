/*
 * MIT License
 *
 *  Copyright (c) 2021. Pela Cristian
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 */

package pcf.crskdev.gitfeed.server.core.net

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.cache.CacheKeys
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.cache.createKey
import pcf.crskdev.gitfeed.server.core.cache.raw
import pcf.crskdev.gitfeed.server.core.cache.switch
import pcf.crskdev.gitfeed.server.core.util.MockTimeline
import pcf.crskdev.gitfeed.server.core.util.obj
import java.net.HttpURLConnection
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal class RequestCommandGraceDurationProxyTest : StringSpec({
    val uri = URI.create("/")
    val timeKey = CacheKeys.Type.TIME.createKey(uri.toString())
    val responseKey = timeKey.switch(CacheKeys.Type.RES)
    val etagKey = timeKey.switch(CacheKeys.Type.ETAG)
    val token = Bearer("foo")
    val headers = headers(token.asHeader) {
        "Content-Type" to "application/json"
    }

    "it should store timestamp" {
        val cache = mock<CacheStore>()
        val command = mock<RequestCommand>()
        val timeLine = MockTimeline(LocalDateTime.now())
        val mediator = RequestCommandGraceDurationProxy(
            cache,
            command,
            Duration.of(10, ChronoUnit.MINUTES),
            timeLine::now
        )
        val client = RequestClientImpl(mediator, mediator, token)

        whenever(command.request(uri, headers)).thenReturn(
            Response(
                HttpURLConnection.HTTP_OK,
                obj { "msg" to "hello" }.asString(),
                headers { "ETag" to "etag_123" }
            )
        )

        client.request<Map<String, String>>(uri) { it.body }

        verify(cache, times(2))[timeKey.raw()] = timeLine.now().toString()
        verify(cache)[responseKey.raw()] = obj { "msg" to "hello" }.asString()
        verify(cache)[etagKey.raw()] = "etag_123"
    }

    "should not forward to remote server if is cache still fresh" {
        val cache = mock<CacheStore>()
        val command = mock<RequestCommand>()
        val now = LocalDateTime.now()
        val timeLine = MockTimeline(now)
        val mediator = RequestCommandGraceDurationProxy(
            cache,
            command,
            Duration.of(10, ChronoUnit.MINUTES),
            timeLine::now
        )
        val client = RequestClientImpl(mediator, mediator, token)

        whenever(cache[timeKey.raw()])
            .thenReturn(now.minusMinutes(5).toString())
        whenever(cache[etagKey.raw()]).thenReturn("etag_123")
        whenever(cache[responseKey.raw()]).thenReturn(
            obj { "msg" to "hello" }.asString()
        )
        whenever(cache.exists(responseKey.raw())).thenReturn(true)

        val result = client.request<Map<String, String>>(uri) { it.body }

        result shouldBe mapOf("msg" to "hello")

        verify(command, never()).request(any(), any())
        verify(cache, never())[timeKey.raw()] = timeLine.now().toString()
        verify(cache, never())[responseKey.raw()] = obj { "msg" to "hello" }.asString()
        verify(cache, never())[etagKey.raw()] = "etag_123"
    }

    "should forward to remote server if is cache is old enough" {
        val cache = mock<CacheStore>()
        val command = mock<RequestCommand>()
        val now = LocalDateTime.now()
        val timeLine = MockTimeline(now)
        val mediator = RequestCommandGraceDurationProxy(
            cache,
            command,
            Duration.of(10, ChronoUnit.MINUTES),
            timeLine::now
        )
        val client = RequestClientImpl(mediator, mediator, token)

        whenever(cache[timeKey.raw()])
            .thenReturn(now.toString())
        whenever(cache[etagKey.raw()]).thenReturn("etag_123")
        whenever(cache[responseKey.raw()]).thenReturn(
            obj { "msg" to "hello" }.asString()
        )
        whenever(cache.exists(responseKey.raw())).thenReturn(true)
        whenever(command.request(uri, headers(headers) { "If-None-Match" to "etag_123" })).thenReturn(
            Response(
                HttpURLConnection.HTTP_OK,
                obj { "msg" to "hello" }.asString(),
                headers { "ETag" to "etag_124" }
            )
        )

        timeLine.advanceBy(11, ChronoUnit.MINUTES)

        val result = client.request<Map<String, String>>(uri) { it.body }

        result shouldBe mapOf("msg" to "hello")

        verify(cache, times(2))[timeKey.raw()] = timeLine.now().toString()
        verify(cache)[responseKey.raw()] = obj { "msg" to "hello" }.asString()
        verify(cache)[etagKey.raw()] = "etag_124"
    }

    "should forward to remote server if timestamp is not in cache cache" {
        val cache = mock<CacheStore>()
        val command = mock<RequestCommand>()
        val now = LocalDateTime.now()
        val timeLine = MockTimeline(now)
        val mediator = RequestCommandGraceDurationProxy(
            cache,
            command,
            Duration.of(10, ChronoUnit.MINUTES),
            timeLine::now
        )
        val client = RequestClientImpl(mediator, mediator, token)

        whenever(cache[etagKey.raw()]).thenReturn("etag_123")
        whenever(cache[responseKey.raw()]).thenReturn(
            obj { "msg" to "hello" }.asString()
        )
        whenever(cache.exists(responseKey.raw())).thenReturn(true)
        whenever(command.request(uri, headers(headers) { "If-None-Match" to "etag_123" })).thenReturn(
            Response(
                HttpURLConnection.HTTP_NOT_MODIFIED,
                obj { "msg" to "hello" }.asString(),
                headers { "ETag" to "etag_123" }
            )
        )

        timeLine.advanceBy(11, ChronoUnit.MINUTES)

        val result = client.request<Map<String, String>>(uri) { it.body }

        result shouldBe mapOf("msg" to "hello")

        verify(cache, times(1))[timeKey.raw()] = timeLine.now().toString()
        verify(cache, never())[responseKey.raw()] = obj { "msg" to "hello" }.asString()
        verify(cache, never())[etagKey.raw()] = "etag_123"
    }
})
