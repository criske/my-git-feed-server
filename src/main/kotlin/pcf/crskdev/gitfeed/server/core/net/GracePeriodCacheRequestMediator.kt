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

import pcf.crskdev.gitfeed.server.core.cache.CacheKeys
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.cache.createKey
import pcf.crskdev.gitfeed.server.core.cache.raw
import pcf.crskdev.gitfeed.server.core.cache.switch
import java.net.HttpURLConnection
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime

class GracePeriodCacheRequestMediator(
    private val duration: Duration,
    private val cache: CacheStore,
    private val command: RequestCommand,
    private val timeProvider: () -> LocalDateTime = { LocalDateTime.now() }
) : CacheStore by cache, RequestCommand by command {

    override fun set(key: String, value: String) {
        val timeKey = CacheKeys
            .getFromRaw(key)
            .switch(CacheKeys.Type.TIME)
            .raw()
        this.cache[key] = value
        this.cache[timeKey] = this.nowStr()
    }

    override fun request(uri: URI, headers: Headers): Response {
        return if (headers.containsKey("If-None-Match")) {
            val responseKey = CacheKeys.Type.RESPONSE.createKey(uri.toString())
            if (this.cache.exists(responseKey.raw())) {
                val timeKey = responseKey.switch(CacheKeys.Type.TIME).raw()
                val time = this.cache[timeKey]
                if (time != null) {
                    val isStillFresh = Duration
                        .between(this.now(), LocalDateTime.parse(time))
                        .minus(this.duration)
                        .isNegative
                    // if cache is not that old, don't bother to check the remote server
                    // for resource modification.
                    if (isStillFresh) {
                        Response(HttpURLConnection.HTTP_NOT_MODIFIED, null, emptyMap())
                    } else {
                        this.command.request(uri, headers)
                    }
                } else {
                    this.command.request(uri, headers).apply {
                        if (this.code == HttpURLConnection.HTTP_NOT_MODIFIED) {
                            // old cache doesn't have a time key due to legacy impl or eviction
                            // we set cache timestamp here.
                            cache[timeKey] = nowStr()
                        }
                    }
                }
            } else {
                this.command.request(uri, headers)
            }
        } else {
            this.command.request(uri, headers)
        }
    }

    private fun now() = this.timeProvider()

    private fun nowStr() = now().toString()
}
