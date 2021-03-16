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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import pcf.crskdev.gitfeed.server.core.net.first
import pcf.crskdev.gitfeed.server.core.net.headers
import java.net.URI

internal class RestTemplateCommandTest : StringSpec({

    "rest template request command impl should work" {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setBody("Hello")
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json".toMediaType())
        )
        server.start()

        val url = URI.create(server.url("/api").toString())

        val reqCommand = RestTemplateCommand()
        val res = reqCommand.request(
            url,
            headers {
                "Content-Type" to "application/json"
                "Authorization" to "Bearer 123"
            }
        )
        val req = server.takeRequest()

        req.getHeader("Content-Type") shouldBe "application/json"
        req.getHeader("Authorization") shouldBe "Bearer 123"
        res.body shouldBe "Hello"
        res.code shouldBe 200
        res.headers.first("Content-Type") shouldBe "application/json"

        server.shutdown()
    }
})
