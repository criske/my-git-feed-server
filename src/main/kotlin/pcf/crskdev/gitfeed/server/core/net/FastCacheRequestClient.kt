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

import com.fasterxml.jackson.databind.JsonNode
import pcf.crskdev.gitfeed.server.core.util.logger
import java.net.URI

/**
 * Fast cache request client.
 *
 * @param delegate RequestClient.
 *
 * @author Cristian Pela
 */
internal class FastCacheRequestClient(private val delegate: RequestClient) : RequestClient {

    /**
     * Logger.
     */
    private val logger by logger()

    /**
     * Fast cache.
     */
    private val fastCache = mutableMapOf<URI, Any>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> request(
        uri: URI,
        extraHeaders: Headers,
        clazz: Class<T>,
        responseMapper: (JsonResponse) -> JsonNode
    ): T = if (!fastCache.containsKey(uri)) {
        logger.info {
            "Storing to fast cache for $uri"
        }
        val entry = this.delegate.request(uri, extraHeaders, clazz, responseMapper) as Any
        fastCache[uri] = entry
        entry as T
    } else {
        logger.info {
            "Fetch from fast cache for $uri"
        }
        fastCache[uri]!! as T
    }

    override fun authorized(accessToken: AccessToken): RequestClient =
        FastCacheRequestClient(this.delegate.authorized(accessToken))

    override fun fastCache(): RequestClient = this
}
