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

package pcf.crskdev.gitfeed.server.impl.core.net

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.net.Headers
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.Response
import pcf.crskdev.gitfeed.server.core.toGitFeedException
import java.io.BufferedReader
import java.io.IOException
import java.net.URI

/**
 * RequestCommand implementation based on [RestTemplate].
 *
 * @author Cristian Pela
 */
class RestTemplateCommand : RequestCommand {

    private val restTemplate = RestTemplateBuilder()
        .errorHandler(object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean {
                return (
                    response.statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
                        response.statusCode.series() == HttpStatus.Series.SERVER_ERROR
                    )
            }

            override fun handleError(response: ClientHttpResponse) {
                val gitFeedException = try {
                    GitFeedException.fromString(
                        type = GitFeedException.Type.HTTP,
                        response.body.bufferedReader().use(BufferedReader::readText),
                        true
                    )
                } catch (ex: IOException) {
                    ex.toGitFeedException()
                }
                throw gitFeedException
            }
        })
        .build()

    override fun request(uri: URI, headers: Headers): Response {
        val restHeaders = RestHeaders().apply {
            headers.forEach { (key, values) ->
                addAll(key, values)
            }
        }
        val entity = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            HttpEntity<String>(restHeaders),
            String::class.java
        )
        return Response(
            entity.statusCode.value(),
            entity.body, entity.headers.toMap()
        )
    }
}

typealias RestHeaders = org.springframework.http.HttpHeaders
