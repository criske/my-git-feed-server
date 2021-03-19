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
import pcf.crskdev.gitfeed.server.core.util.base64Encode
import java.net.HttpURLConnection
import java.net.URI

/**
 * Request client.
 *
 * @author Cristian Pela
 */
interface RequestClient {

    /**
     * Make a request and also deals with caching.
     *
     * @param T type of response body class.
     * @param uri URI.
     * @param extraHeaders Although RequestClient takes of the all the required headers,
     * one might add extra request headers.
     * @param clazz Class of T needed to construct the object.
     * @param responseMapper
     * @receiver
     * @return T
     */
    fun <T> request(
        uri: URI,
        extraHeaders: Headers = emptyMap(),
        clazz: Class<T>,
        responseMapper: (JsonResponse) -> JsonNode = { it.body }
    ): T

    /**
     * Authorized request client.
     *
     * @param accessToken AccessToken.
     * @return RequestClient
     */
    fun authorized(accessToken: AccessToken): RequestClient

    /**
     * Request client the uses a local in memory cache, without caring about
     * the data staleness.
     *
     * This should only be used in locally, for example when doing successive
     * requests for the same url(s) within a function scope.
     *
     * This client should not be used in a multi-thread environment.
     *
     * @return RequestClient
     */
    fun fastCache(): RequestClient
}

/**
 * Request client.
 *
 * @property cache Cache Store.
 * @property requestCommand Request Command.
 * @property accessToken Access Token.
 *
 * @author Cristian Pela
 */
class RequestClientImpl(
    private val cache: CacheStore,
    private val requestCommand: RequestCommand,
    private val accessToken: AccessToken = Unauthorized
) : RequestClient {

    /**
     * Object mapper.
     */
    private val objectMapper = KObjectMapper

    /**
     * Make a request and also deals with caching.
     *
     * @param T type of response body class.
     * @param uri URI.
     * @param extraHeaders Although RequestClient takes of the all the required headers,
     * one might add extra request headers.
     * @param clazz Class of T needed to construct the object.
     * @param responseMapper
     * @receiver
     * @return T
     */
    override fun <T> request(
        uri: URI,
        extraHeaders: Headers,
        clazz: Class<T>,
        responseMapper: (JsonResponse) -> JsonNode
    ): T {

        val baseHeaders = headers(extraHeaders) {
            "Content-Type" to "application/json"
            accessToken.key to accessToken.value
        }

        val headersWithCache = this.cache[this.etagKey(uri)]?.let { etag ->
            headers(baseHeaders) { "If-None-Match" to etag }
        } ?: baseHeaders

        val response = this.requestCommand.request(uri, headersWithCache)

        return when (response.code) {
            HttpURLConnection.HTTP_OK -> {
                // TODO use proper logging here
                println("REQUEST CLIENT: from remote: $uri")
                this.processResponse(uri, response, clazz, responseMapper)
            }
            HttpURLConnection.HTTP_NOT_MODIFIED -> {
                val cachedResponse = this.cache[responseKey(uri)]
                if (cachedResponse == null) { // missed cache just in case
                    val newResponse = this.requestCommand.request(
                        uri,
                        baseHeaders
                    )
                    if (newResponse.code == HttpURLConnection.HTTP_OK) {
                        this.processResponse(uri, newResponse, clazz, responseMapper)
                    } else {
                        throw GitFeedException.fromString(Type.HTTP, newResponse.body, true)
                    }
                } else {
                    println("REQUEST CLIENT: from cache: $uri")
                    this.objectMapper.readValue(cachedResponse, clazz)
                }
            }
            else -> throw GitFeedException.fromString(Type.HTTP, response.body, true)
        }
    }

    override fun authorized(accessToken: AccessToken): RequestClient =
        RequestClientImpl(this.cache, this.requestCommand, accessToken)

    override fun fastCache(): RequestClient = FastCacheRequestClient(this)

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
    private fun etagKey(uri: URI): String =
        base64Encode("etag", uri.toString(), padded = false)

    /**
     * Response key cache.
     *
     * @param uri URI
     * @return String.
     */
    private fun responseKey(uri: URI): String =
        base64Encode("res", uri.toString(), padded = false)


    /**
     * Fast cache request client.
     *
     * @param delegate RequestClient.
     */
    private class FastCacheRequestClient(private val delegate: RequestClient) : RequestClient {

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
        ): T = fastCache
            .apply {
                if(containsKey(uri)){
                    println("REQUEST CLIENT: from fast cache: $uri")
                }
            }.computeIfAbsent(uri) {
                this.delegate.request(uri, extraHeaders, clazz, responseMapper) as Any
            } as T

        override fun authorized(accessToken: AccessToken): RequestClient =
            FastCacheRequestClient(this.delegate.authorized(accessToken))

        override fun fastCache(): RequestClient = this
    }
}

/**
 * Reified extension of [RequestClient.request].
 *
 * @param T type of response body class.
 * @param uri URI.
 * @param extraHeaders Although RequestClient takes of the all the required headers,
 * one might add extra request headers.
 * @param responseMapper
 * @receiver
 * @return T
 */
inline fun <reified T> RequestClient.request(
    uri: URI,
    extraHeaders: Headers = emptyMap(),
    noinline responseMapper: (JsonResponse) -> JsonNode = { it.body }
): T = this.request(uri, extraHeaders, T::class.java, responseMapper)
