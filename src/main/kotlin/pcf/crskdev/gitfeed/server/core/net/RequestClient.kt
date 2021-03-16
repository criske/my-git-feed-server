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
import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.GitFeedException.Type
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.util.KObjectMapper
import java.net.HttpURLConnection
import java.net.URI

/**
 * Request client.
 *
 * @property cache Cache Store.
 * @property requestCommand Request Command.
 * @property accessToken Access Token.
 *
 * @author Cristian Pela
 */
class RequestClient(
    @PublishedApi
    internal val cache: CacheStore,
    @PublishedApi
    internal val requestCommand: RequestCommand,
    @PublishedApi
    internal val accessToken: AccessToken = AccessToken.UNAUTHORIZED
) {

    @PublishedApi
    internal val objectMapper = KObjectMapper()

    /**
     * Make a request and also deals with caching.
     *
     * @param T type of response body class.
     * @param uri URI.
     * @param extraHeaders Although RequestClient takes of the all the required headers,
     * one might add extra request headers.
     * @param responseMapper
     * @receiver
     * @return T
     */
    inline fun <reified T> request(
        uri: URI,
        extraHeaders: Headers = emptyMap(),
        noinline responseMapper: (JsonResponse) -> JsonNode = { it.body }
    ): T {

        val baseHeaders = headers(extraHeaders) {
            "Content-Type" to "application/json"
            accessToken.key to accessToken.value
        }

        val headersWithCache = this.cache[etagKey(uri)]?.let { etag ->
            headers(baseHeaders) { "If-None-Match" to etag }
        } ?: baseHeaders

        val response = this.requestCommand.request(uri, headersWithCache)

        return when (response.code) {
            HttpURLConnection.HTTP_OK -> {
                this.processResponse(uri, response, T::class.java, responseMapper)
            }
            HttpURLConnection.HTTP_NOT_MODIFIED -> {
                val cachedResponse = this.cache[responseKey(uri)]
                if (cachedResponse == null) { // missed cache just in case
                    val newResponse = this.requestCommand.request(
                        uri,
                        baseHeaders
                    )
                    if (newResponse.code == HttpURLConnection.HTTP_OK) {
                        this.processResponse(uri, newResponse, T::class.java, responseMapper)
                    } else {
                        throw GitFeedException(Type.HTTP, newResponse.body)
                    }
                } else {
                    this.objectMapper.readValue(cachedResponse, T::class.java) as T
                }
            }
            else -> throw GitFeedException(Type.HTTP, response.body)
        }
    }

    fun authorized(accessToken: AccessToken): RequestClient =
        RequestClient(this.cache, this.requestCommand, accessToken)

    @PublishedApi
    internal fun <T> processResponse(
        uri: URI,
        response: Response,
        clazz: Class<T>,
        responseMapper: (JsonResponse) -> JsonNode
    ): T {
        val json = this
            .objectMapper
            .readTree(response.body!!)
        val jsonResponse = JsonResponse(json, response.headers)
        val jsonMapped = responseMapper(jsonResponse)
        val strMapped = this.objectMapper.writeValueAsString(jsonMapped)
        response.headers.first("ETag")?.also { etag ->
            this.cache[this.etagKey(uri)] = etag
            this.cache[this.responseKey(uri)] = strMapped
        }
        return this.objectMapper.readValue(strMapped, clazz)
    }

    /**
     * Etag key cache.
     *
     * @param uri URI.
     * @return String.
     */
    @PublishedApi
    internal fun etagKey(uri: URI): String = "etag\$$uri"

    /**
     * Response key cache.
     *
     * @param uri URI
     * @return String.
     */
    @PublishedApi
    internal fun responseKey(uri: URI): String = "res\$$uri"
}
