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

package pcf.crskdev.gitfeed.server.core.feed.github

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.feed.models.Commit
import pcf.crskdev.gitfeed.server.core.feed.models.Owner
import pcf.crskdev.gitfeed.server.core.feed.models.Paging
import pcf.crskdev.gitfeed.server.core.feed.models.Repo
import pcf.crskdev.gitfeed.server.core.net.Bearer
import pcf.crskdev.gitfeed.server.core.net.RequestClientImpl
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.Response
import pcf.crskdev.gitfeed.server.core.net.headers
import java.io.File
import java.net.URI

internal class GithubGitFeedTest : StringSpec({

    "should fetch commits" {
        val uri = URI.create("https://api.github.com/search/commits?q=author:criske&sort=author-date")
        val requestHeaders = headers {
            "Accept" to "application/vnd.github.cloak-preview+json"
            "Content-Type" to "application/json"
            "Authorization" to "Bearer 123"
        }
        val command = mock<RequestCommand>()
        val gitFeed = GithubGitFeed(RequestClientImpl(mock(), command, Bearer("123")))

        whenever(command.request(uri, requestHeaders)).thenReturn(
            Response(
                200,
                File("src/test/resources/github_commits.json").readText(),
                headers {
                    "Link" to "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=2>; rel=\"next\"," +
                        "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=34>; rel=\"last\""
                }
            )
        )

        val commits = gitFeed.commits()

        commits.paging shouldBe Paging(next = 2, last = 34)
        commits.entries.size shouldBe 10
        commits.entries.first() shouldBe Commit(
            "d4496c7",
            "2021-03-13T15:58:06.000+02:00",
            "https://github.com/self-xdsd/self-web/commit/d4496c7c50e6ae443263e5fe5c13b631ca2cbebd",
            "#371 Allow Deactivation Of All PaymentMethods.",
            Repo(
                "self-xdsd/self-web",
                "https://github.com/self-xdsd/self-web",
                Owner(
                    "self-xdsd",
                    "https://avatars.githubusercontent.com/u/65442807?v=4",
                    "https://github.com/self-xdsd"
                )
            )
        )
    }

    "should fetch page commits" {
        val uri = URI.create("https://api.github.com/search/commits?q=author:criske&sort=author-date&page=2")
        val requestHeaders = headers {
            "Accept" to "application/vnd.github.cloak-preview+json"
            "Content-Type" to "application/json"
            "Authorization" to "Bearer 123"
        }
        val command = mock<RequestCommand>()
        val gitFeed = GithubGitFeed(RequestClientImpl(mock(), command, Bearer("123")))

        whenever(command.request(uri, requestHeaders)).thenReturn(
            Response(
                200,
                File("src/test/resources/github_commits.json").readText(),
                emptyMap()
            )
        )

        gitFeed.commits(2)

        verify(command).request(uri, requestHeaders)
    }
})
