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

package pcf.crskdev.gitfeed.server.core.feed.gitlab

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.feed.models.Paging
import pcf.crskdev.gitfeed.server.core.feed.models.Repo
import pcf.crskdev.gitfeed.server.core.feed.models.RepoExtended
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.net.Bearer
import pcf.crskdev.gitfeed.server.core.net.RequestClientImpl
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.Response
import pcf.crskdev.gitfeed.server.core.net.headers
import java.io.File
import java.net.URI

internal class GitlabGitFeedTest : StringSpec({

    "should get me" {
        val uri = URI.create("https://gitlab.com/api/v4/user")
        val command = mock<RequestCommand>()
        val requestHeaders = headers {
            "Content-Type" to "application/json"
            "Authorization" to "Bearer fake_123"
        }
        val gitFeed = GitlabGitFeed(
            RequestClientImpl(mock(), command, Bearer("fake_123"))
        )
        whenever(command.request(uri, requestHeaders)).thenReturn(
            Response(
                200,
                File("src/test/resources/gitlab_me.json").readText(),
                emptyMap()
            )
        )

        gitFeed.me() shouldBe User(
            "criske",
            "https://secure.gravatar.com/avatar/d50208500191eb9b0d99ed29be6facd5?s=80&d=identicon",
            "https://gitlab.com/criske",
            "User",
            "Gitlab"
        )
    }

    "should get owned repos (not forked)" {
        val uri = URI.create("https://gitlab.com/api/v4/users/6018288/projects?visibility=public&per_page=100&page=1")
        val command = mock<RequestCommand>()
        val requestHeaders = headers {
            "Content-Type" to "application/json"
            "Authorization" to "Bearer fake_123"
        }
        val gitFeed = GitlabGitFeed(
            RequestClientImpl(mock(), command, Bearer("fake_123"))
        )
        whenever(command.request(uri, requestHeaders)).thenReturn(
            Response(
                200,
                File("src/test/resources/gitlab_repos.json").readText(),
                emptyMap()
            )
        )

        val repos = gitFeed.repos()

        repos.entries.size shouldBe 2
        repos.paging shouldBe Paging()
        repos.entries.first() shouldBe RepoExtended(
            Repo(
                "self-xdsd-playground",
                "criske/self-xdsd-playground",
                "https://gitlab.com/criske/self-xdsd-playground",
                User(
                    "criske",
                    "https://secure.gravatar.com/avatar/d50208500191eb9b0d99ed29be6facd5?s=80&d=identicon",
                    "https://gitlab.com/criske",
                    "User",
                    "Gitlab"
                )
            ),
            "Playground to test Gitlab API for Self https://github.com/self-xdsd",
            false,
            false,
            1,
            null,
            null,
            "2020-11-25T12:34:35.706Z",
            "2021-01-26T12:56:14.310Z"
        )
    }
})
