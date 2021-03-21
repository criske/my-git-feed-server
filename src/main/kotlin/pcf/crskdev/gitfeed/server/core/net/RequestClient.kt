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
