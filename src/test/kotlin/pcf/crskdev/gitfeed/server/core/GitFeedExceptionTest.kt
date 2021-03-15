package pcf.crskdev.gitfeed.server.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class GitFeedExceptionTest : StringSpec({

    "should use simple message as error content" {
        val err = GitFeedException(GitFeedException.Type.VALIDATION, "field required")
        err.message shouldBe """{"type":"validation","error":"field required"}"""
    }

    "should use simple message as error content array" {
        val err = GitFeedException(GitFeedException.Type.IO, """["error1",{"error2":{"msg":"whoops"}}]""")
        err.message shouldBe """{"type":"io","error":["error1",{"error2":{"msg":"whoops"}}]}"""
    }

    "should try to convert input message to json" {
        val err = GitFeedException(
            GitFeedException.Type.HTTP,
            "{\n" +
                "\"message\": \"Requires authentication\"," +
                "\"documentation_url\": \"https://docs.github.com/rest/reference/users#get-the-authenticated-user\"" +
                "}"
        )
        err.message shouldBe """{"type":"http","error":{"message":"Requires authentication","documentation_url":"https://docs.github.com/rest/reference/users#get-the-authenticated-user"}}"""
    }
})
