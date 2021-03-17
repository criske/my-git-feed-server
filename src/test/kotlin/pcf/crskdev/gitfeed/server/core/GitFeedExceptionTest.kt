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

package pcf.crskdev.gitfeed.server.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.GitFeedException.Type
import pcf.crskdev.gitfeed.server.core.util.obj

internal class GitFeedExceptionTest : StringSpec({

    "should use simple message as error content" {
        val err = GitFeedException.fromString(Type.VALIDATION, "field required")
        err.message shouldBe """{"type":"validation","error":"field required"}"""
    }

    "should use simple message as error content array" {
        val err = GitFeedException.fromString(Type.IO, """["error1",{"error2":{"msg":"whoops"}}]""", true)
        err.message shouldBe """{"type":"io","error":["error1",{"error2":{"msg":"whoops"}}]}"""
    }

    "should try to convert input message to json" {
        val err = GitFeedException.fromString(
            Type.HTTP,
            "{\n" +
                "\"message\": \"Requires authentication\"," +
                "\"documentation_url\": \"https://docs.github.com/rest/reference/users#get-the-authenticated-user\"" +
                "}",
            true
        )
        err.message shouldBe """{"type":"http","error":{"message":"Requires authentication","documentation_url":"https://docs.github.com/rest/reference/users#get-the-authenticated-user"}}"""
    }

    "should use json node as message" {
        val message = obj {
            "field" to "invalid field"
        }.asTree()
        val err = GitFeedException(Type.VALIDATION, message)
        err.asJson() shouldBe obj {
            "type" to "validation"
            "error" to obj {
                "field" to "invalid field"
            }
        }.asTree()
        err.message shouldBe """{"type":"validation","error":{"field":"invalid field"}}"""
    }
})
