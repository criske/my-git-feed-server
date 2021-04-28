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

package pcf.crskdev.gitfeed.server.core.feed.bitbucket

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.net.Basic
import pcf.crskdev.gitfeed.server.core.net.RequestClientImpl
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.Response
import pcf.crskdev.gitfeed.server.core.net.headers
import java.io.File
import java.net.URI

internal class BitbucketGitFeedTest : StringSpec({

    "should get commits" {
        val command = mock<RequestCommand>()
        val gitFeed = BitbucketGitFeed(
            RequestClientImpl(mock(), command, Basic.withEncoded("123"))
        )
        shouldThrow<NotImplementedError> {
            gitFeed.commits()
        }
    }

    "should get assignments" {
        val command = mock<RequestCommand>()
        val gitFeed = BitbucketGitFeed(
            RequestClientImpl(mock(), command, Basic.withEncoded("123"))
        )
        shouldThrow<NotImplementedError> {
            gitFeed.assignments()
        }
    }

    "should get me" {
        val uri = URI.create("https://bitbucket.org/api/2.0/user")
        val command = mock<RequestCommand>()
        val requestHeaders = headers {
            "Content-Type" to "application/json"
            "Authorization" to "Basic fake_123"
        }
        val gitFeed = BitbucketGitFeed(
            RequestClientImpl(mock(), command, Basic.withEncoded("fake_123"))
        )
        whenever(command.request(uri, requestHeaders)).thenReturn(
            Response(
                200,
                File("src/test/resources/bitbucket_me.json").readText(),
                emptyMap()
            )
        )

        gitFeed.me() shouldBe User(
            "cristianpela",
            "https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/557058:0f30dbbe-e90b-4d4a-a005-6fc83820c8e7/30024e46-ecc6-4fc3-9275-c97e2a8b8418/128",
            "https://bitbucket.org/%7Bc94b65af-2573-4c7a-93ad-da943c45ecaf%7D/",
            "user",
            "Bitbucket"
        )
    }

    "should get repos" {
        val command = mock<RequestCommand>()
        val gitFeed = BitbucketGitFeed(
            RequestClientImpl(mock(), command, Basic.withEncoded("123"))
        )
        shouldThrow<NotImplementedError> {
            gitFeed.repos()
        }
    }
})
