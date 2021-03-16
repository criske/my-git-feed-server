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
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.test.context.ContextConfiguration
import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.util.kotest.getBean
import pcf.crskdev.gitfeed.server.util.spring.TestBeanFactories
import java.net.URI

@ContextConfiguration(classes = [TestBeanFactories::class])
internal class RequestClientTest(private val client: RequestClient) : DescribeSpec() {

    init {
        describe("client injection tests") {
            it("should be injected") {
                client.shouldBeInstanceOf<RequestClient>()
            }
        }
        describe("network tests") {
            val requestCommand = getBean<RequestCommand>()
            beforeTest {
                reset(requestCommand)
            }
            it("should get OK response") {
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(200, """{"message":"hello"}""", emptyMap())
                )
                val message = client.request<Message>(URI.create("/"))
                message shouldBe Message("hello")
            }
            it("should throw if response is not OK") {
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(401, "", emptyMap())
                )
                shouldThrow<GitFeedException> {
                    client.request<Message>(URI.create("/"))
                }
            }
        }
        describe("cache tests") {

            val cache = getBean<CacheStore>()
            val requestCommand = getBean<RequestCommand>()

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
                client.request<Message>(URI.create("/"))
                verify(cache, times(1))["etag$/"] = "123"
                verify(cache, times(1))["res$/"] = """{"message":"hello"}"""
            }

            it("should get from cache") {
                whenever(requestCommand.request(any(), any())).thenReturn(
                    Response(304, null, emptyMap())
                )
                whenever(cache["etag$/"]).thenReturn("123")
                whenever(cache["res$/"]).thenReturn("""{"message":"hello"}""")
                val message = client.request<Message>(URI.create("/"))
                message shouldBe Message("hello")
                verify(requestCommand, times(1)).request(
                    URI.create("/"),
                    headers {
                        "Content-Type" to "application/json"
                        "If-None-Match" to "123"
                        "Authorization" to ""
                    }
                )
            }

            it("should get new request if cache is missing") {
                whenever(cache["etag$/"]).thenReturn("123")
                whenever(
                    requestCommand.request(
                        URI.create("/"),
                        headers {
                            "Content-Type" to "application/json"
                            "If-None-Match" to "123"
                            "Authorization" to ""
                        }
                    )
                ).thenReturn(Response(304, null, emptyMap()))
                whenever(
                    requestCommand.request(
                        URI.create("/"),
                        headers {
                            "Content-Type" to "application/json"
                            "Authorization" to ""
                        }
                    )
                ).thenReturn(
                    Response(
                        200,
                        """{"message":"hello"}""",
                        headers { "ETag" to "123" }
                    )
                )

                val message = client.request<Message>(URI.create("/"))
                message shouldBe Message("hello")
            }

            it("should throw if response after missing cache is not OK") {
                whenever(cache["etag$/"]).thenReturn("123")
                whenever(
                    requestCommand.request(
                        URI.create("/"),
                        headers {
                            "Content-Type" to "application/json"
                            "If-None-Match" to "123"
                            "Authorization" to ""
                        }
                    )
                ).thenReturn(Response(304, null, emptyMap()))
                whenever(
                    requestCommand.request(
                        URI.create("/"),
                        headers {
                            "Content-Type" to "application/json"
                            "Authorization" to ""
                        }
                    )
                ).thenReturn(
                    Response(500, null, emptyMap())
                )

                shouldThrow<GitFeedException> {
                    client.request<Message>(URI.create("/"))
                }
            }
        }
    }

    data class Message(val message: String)
}
