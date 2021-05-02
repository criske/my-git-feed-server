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

@file:Suppress("BlockingMethodInNonBlockingContext")

package pcf.crskdev.gitfeed.server.core.net

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.cache.CacheKeys
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.cache.createKey
import pcf.crskdev.gitfeed.server.core.cache.raw
import pcf.crskdev.gitfeed.server.core.cache.switch
import java.net.URI

internal class RequestClientTest() : DescribeSpec() {

    init {
        val uri = URI.create("http://foo.com")
        val etagKey = CacheKeys.Type.ETAG.createKey(uri.toString())
        val resKey = etagKey.switch(CacheKeys.Type.RES)

        val requestCommand = mock<RequestCommand>()
        val cache = mock<CacheStore>()
        val client = RequestClientImpl(cache, requestCommand)
            .authorized(Bearer("123"))

        describe("network tests") {
            beforeTest {
                reset(requestCommand)
            }
            it("should throw if access token header is not set") {
                shouldThrow<GitFeedException> {
                    client.authorized(Unauthorized).request<Message>(uri)
                }
            }
            it("should get OK response") {
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(200, """{"message":"hello"}""", emptyMap())
                )
                val message = client.request<Message>(uri)
                message shouldBe Message("hello")
            }
            it("should throw if response is not OK") {
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(401, "", emptyMap())
                )
                shouldThrow<GitFeedException> {
                    client.request<Message>(uri)
                }
            }
        }
        describe("cache tests") {
            beforeTest {
                reset(cache, requestCommand)
            }
            it("should cache the response") {
                val etag = "123"
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(
                        200,
                        """{"message":"hello"}""",
                        headers { "ETag" to etag }
                    )
                )
                client.request<Message>(uri)
                verify(cache, times(1))[etagKey.raw()] = "123"
                verify(cache, times(1))[resKey.raw()] = """{"message":"hello"}"""
            }

            it("should get from cache") {
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(304, null, emptyMap())
                )
                whenever(cache[etagKey.raw()]).thenReturn("123")
                whenever(cache[resKey.raw()]).thenReturn("""{"message":"hello"}""")
                val message = client.request<Message>(uri)
                message shouldBe Message("hello")
                verify(requestCommand, times(1)).request(
                    uri,
                    headers {
                        "Content-Type" to "application/json"
                        "If-None-Match" to "123"
                        "Authorization" to "Bearer 123"
                    }
                )
            }

            it("should get new request if cache is missing") {
                whenever(cache[etagKey.raw()]).thenReturn("123")
                whenever(
                    requestCommand.request(
                        uri,
                        headers {
                            "Content-Type" to "application/json"
                            "If-None-Match" to "123"
                            "Authorization" to "Bearer 123"
                        }
                    )
                ).thenReturn(Response(304, null, emptyMap()))
                whenever(
                    requestCommand.request(
                        uri,
                        headers {
                            "Content-Type" to "application/json"
                            "Authorization" to "Bearer 123"
                        }
                    )
                ).thenReturn(
                    Response(
                        200,
                        """{"message":"hello"}""",
                        headers { "ETag" to "123" }
                    )
                )

                val message = client.request<Message>(uri)
                message shouldBe Message("hello")
            }

            it("should throw if response after missing cache is not OK") {
                whenever(cache[etagKey.raw()]).thenReturn("123")
                whenever(
                    requestCommand.request(
                        uri,
                        headers {
                            "Content-Type" to "application/json"
                            "If-None-Match" to "123"
                            "Authorization" to "Bearer 123"
                        }
                    )
                ).thenReturn(Response(304, null, emptyMap()))
                whenever(
                    requestCommand.request(
                        uri,
                        headers {
                            "Content-Type" to "application/json"
                            "Authorization" to "Bearer 123"
                        }
                    )
                ).thenReturn(
                    Response(500, null, emptyMap())
                )

                shouldThrow<GitFeedException> {
                    client.request<Message>(uri)
                }
            }
        }

        describe("fast cache tests") {

            it("should hit fast cache") {

                val cacheStore = mock<CacheStore>()
                val command = mock<RequestCommand>()

                val fastClient = RequestClientImpl(cacheStore, command, Bearer("123"))
                    .fastCache()

                whenever(command.request(any(), any())).thenReturn(
                    Response(
                        200,
                        """{"message":"hello"}""",
                        headers { "ETag" to "123" }
                    )
                )

                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                fastClient.request<Message>(uri)
                val message = fastClient.request<Message>(uri)

                // set etag and response
                verify(cacheStore, times(2))[any()] = any()
                // get etag
                verify(cacheStore, times(1))[any()]

                verify(command, times(1)).request(
                    uri,
                    headers {
                        "Content-Type" to "application/json"
                        "Authorization" to "Bearer 123"
                    }
                )

                message shouldBe Message("hello")
            }
        }
    }

    data class Message(val message: String)
}
