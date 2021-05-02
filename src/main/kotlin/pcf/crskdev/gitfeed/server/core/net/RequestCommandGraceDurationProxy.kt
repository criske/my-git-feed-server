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
import pcf.crskdev.gitfeed.server.core.util.logger
import java.net.HttpURLConnection
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime

/**
 *
 * Request command interceptor for conditional check requests.
 *
 * The main idea is not to execute a remote conditional check ("If-None-Match" header)
 * if cached data is fresher than provided duration.
 *
 * To achieve that, each time a new response data is stored in cache, a timestamp
 * is stored too. Whenever a conditional check is requested, this proxy checks
 * if that timestamp has passed the provided "grace" duration.
 *
 * @property cache Cache store.
 * @property command Real request provider.
 * @property duration Period.
 * @property timeProvider Time provider, user in tests.
 */
class RequestCommandGraceDurationProxy(
    private val cache: CacheStore,
    private val command: RequestCommand,
    private val duration: Duration = Duration.ofMinutes(15),
    private val timeProvider: () -> LocalDateTime = { LocalDateTime.now() }
) : CacheStore by cache, RequestCommand by command {

    /**
     * Logger.
     */
    private val logger by logger()

    override fun set(key: String, value: String) {
        val cacheKey = CacheKeys.getFromRaw(key)
        val timeKey = cacheKey
            .switch(CacheKeys.Type.TIME)
            .raw()
        this.cache[key] = value
        this.cache[timeKey] = this.timeProvider().toString().apply {
            logger.info {
                "Setting timestamp for cache entry \"${cacheKey}\"} at $this."
            }
        }
    }

    override fun request(uri: URI, headers: Headers): Response {
        return if (headers.containsKey("If-None-Match")) {
            val responseKey = CacheKeys.Type.RES.createKey(uri.toString())
            if (this.cache.exists(responseKey.raw())) {
                val timeKey = responseKey.switch(CacheKeys.Type.TIME).raw()
                val time = this.cache[timeKey]
                if (time != null) {
                    val currentDuration = Duration
                        .between(this.timeProvider(), LocalDateTime.parse(time))
                        .abs()
                    val isStillFresh = currentDuration < duration
                    // if cache is not that old, don't bother to check the remote server
                    // for resource modification.
                    if (isStillFresh) {
                        logger.info {
                            "Grace period before conditional-check (\"If-None-Match\") is ${duration.toMinutes()}min." +
                                " For \"$uri\" there are ${duration.minus(currentDuration).toMinutes()}min left." +
                                " Intercepting and returning HTTP_NOT_MODIFIED."
                        }
                        Response(HttpURLConnection.HTTP_NOT_MODIFIED, null, emptyMap())
                    } else {
                        logger.info {
                            "Grace period before conditional-check (\"If-None-Match\") is ${duration.toMinutes()}min." +
                                " For \"$uri\" it has passed by ${currentDuration.toMinutes()}min. " +
                                " Performing remote conditional check."
                        }
                        this.command.request(uri, headers)
                    }
                } else {
                    this.command.request(uri, headers).apply {
                        if (this.code == HttpURLConnection.HTTP_NOT_MODIFIED) {
                            // old cache doesn't have a time key due to legacy impl or eviction
                            // we set cache timestamp here.
                            val nowStr = timeProvider().toString()
                            logger.info {
                                "Timestamp for $uri cached is not set. Setting timestamp at $nowStr."
                            }
                            cache[timeKey] = nowStr
                        }
                    }
                }
            } else {
                // this branch should not happen... the check should be done by request client.
                this.command.request(uri, headers)
            }
        } else {
            logger.info {
                "No conditional check for $uri. Forwarding request command..."
            }
            this.command.request(uri, headers)
        }
    }
}
