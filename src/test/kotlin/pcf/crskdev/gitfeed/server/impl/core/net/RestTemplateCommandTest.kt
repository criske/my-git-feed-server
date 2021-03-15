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
