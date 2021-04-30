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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.feed.models.Commit
import pcf.crskdev.gitfeed.server.core.feed.models.Paging
import pcf.crskdev.gitfeed.server.core.feed.models.Repo
import pcf.crskdev.gitfeed.server.core.feed.models.RepoExtended
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.net.Basic
import pcf.crskdev.gitfeed.server.core.net.RequestClientImpl
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.Response
import pcf.crskdev.gitfeed.server.core.net.headers
import pcf.crskdev.gitfeed.server.core.util.obj
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

internal class BitbucketGitFeedTest : DescribeSpec({

    describe("endpoints") {

        it("should get assignments") {
            val command = mock<RequestCommand>()
            val gitFeed = BitbucketGitFeed(
                RequestClientImpl(mock(), command, Basic.withEncoded("123"))
            )
            shouldThrow<NotImplementedError> {
                gitFeed.assignments()
            }
        }

        it("should get me") {
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
                "User",
                "Bitbucket"
            )
        }

        it("should get repos") {
            val uri = URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/?role=owner&q=is_private=false&pagelen=100&page=1")
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
                    File("src/test/resources/bitbucket_repos.json").readText(),
                    emptyMap()
                )
            )

            val repos = gitFeed.repos()

            repos.paging shouldBe Paging(null, null, 2, 3)
            repos.entries.size shouldBe 10
            repos.entries.first() shouldBe RepoExtended(
                Repo(
                    "Blog",
                    "cristianpela/blog",
                    "https://bitbucket.org/cristianpela/blog",
                    User(
                        "cristianpela",
                        "https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/557058:0f30dbbe-e90b-4d4a-a005-6fc83820c8e7/30024e46-ecc6-4fc3-9275-c97e2a8b8418/128",
                        "https://bitbucket.org/%7Bc94b65af-2573-4c7a-93ad-da943c45ecaf%7D/",
                        "User",
                        "Bitbucket"
                    )
                ),
                "",
                false,
                isPrivate = false,
                null,
                "Ruby",
                null,
                "2015-03-17T09:18:34.361055+00:00",
                "2016-08-10T12:26:04.070910+00:00"
            )

            val uriCaptor = argumentCaptor<URI>()
            verify(command).request(uriCaptor.capture(), eq(requestHeaders))

            uriCaptor.firstValue shouldBe URI.create(
                "https://bitbucket.org/api/2.0/repositories/cristianpela/?role=owner&q=is_private=false&pagelen=100&page=1"
            )
        }

        describe("commits") {

            it("should get all commits for a single repo") {
                val command = mock<RequestCommand>()
                val requestHeaders = headers {
                    "Content-Type" to "application/json"
                    "Authorization" to "Basic fake_123"
                }
                val commitsDir = Paths.get("src/test/resources/bitbucket_commits")
                val repoDir = commitsDir.resolve("sleep-cycle-calculator")
                val gitFeed = BitbucketGitFeed(
                    RequestClientImpl(mock(), command, Basic.withEncoded("fake_123")),
                    mapOf(BitbucketGitFeed.COMMITS_PAGE_SIZE to "2")
                )
                whenever(command.request(any(), eq(requestHeaders)))
                    .thenAnswer {
                        val uri = it.getArgument<URI>(0).toString()
                        val file: Path = when {
                            uri.contains("/commits") -> {
                                val page = uri.split("page=")[1]
                                repoDir.resolve(Paths.get("commit_page$page.json"))
                            }
                            else -> {
                                commitsDir.resolve(Paths.get("repos_single.json"))
                            }
                        }
                        Response(
                            200,
                            file.toFile().readText(),
                            emptyMap()
                        )
                    }

                with(gitFeed.commits(1)) {
                    paging shouldBe Paging(null, null, 2, 2)
                    entries.size shouldBe 2
                    entries.first() shouldBe Commit(
                        "809c426",
                        "2016-07-19T10:47:02+00:00",
                        "https://bitbucket.org/cristianpela/sleep-cycle-calculator/commits/809c4261f8c0665dc7849697669339a7cc33f0e8",
                        ".\n",
                        Repo(
                            "sleep-cycle-calculator",
                            "cristianpela/sleep-cycle-calculator",
                            "https://bitbucket.org/cristianpela/sleep-cycle-calculator",
                            User(
                                "cristianpela",
                                "https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/557058:0f30dbbe-e90b-4d4a-a005-6fc83820c8e7/30024e46-ecc6-4fc3-9275-c97e2a8b8418/128",
                                "https://bitbucket.org/%7Bc94b65af-2573-4c7a-93ad-da943c45ecaf%7D/",
                                "User",
                                "Bitbucket"
                            )
                        )
                    )
                }
                with(gitFeed.commits(2)) {
                    paging shouldBe Paging(1, 1, null, null)
                    entries.size shouldBe 2
                }

                val uriCaptor = argumentCaptor<URI>()
                verify(command, times(3 * 2)).request(uriCaptor.capture(), eq(requestHeaders))
                uriCaptor.allValues shouldBe listOf(
                    URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/?q=is_private=false&pagelen=100&page=1"),
                    URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/sleep-cycle-calculator/commits?pagelen=100&page=1"),
                    URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/sleep-cycle-calculator/commits?pagelen=100&page=2"),
                    URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/?q=is_private=false&pagelen=100&page=1"),
                    URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/sleep-cycle-calculator/commits?pagelen=100&page=1"),
                    URI.create("https://bitbucket.org/api/2.0/repositories/cristianpela/sleep-cycle-calculator/commits?pagelen=100&page=2")
                )
            }

            it("should get commits from multi repo") {
                val command = mock<RequestCommand>()
                val requestHeaders = headers {
                    "Content-Type" to "application/json"
                    "Authorization" to "Basic fake_123"
                }
                val commitsDir = Paths.get("src/test/resources/bitbucket_commits")
                val gitFeed = BitbucketGitFeed(
                    RequestClientImpl(mock(), command, Basic.withEncoded("fake_123")),
                    mapOf(BitbucketGitFeed.COMMITS_PAGE_SIZE to "2")
                )
                whenever(command.request(any(), eq(requestHeaders)))
                    .thenAnswer {
                        val uri = it.getArgument<URI>(0).toString()
                        val file: Path = when {
                            uri.contains("/commits") -> {
                                val page = uri.split("page=")[1]
                                val repoDir = commitsDir.resolve(uri.split("/")[7])
                                repoDir.resolve(Paths.get("commit_page$page.json"))
                            }
                            else -> {
                                commitsDir.resolve(Paths.get("repos.json"))
                            }
                        }
                        Response(
                            200,
                            file.toFile().readText(),
                            emptyMap()
                        )
                    }

                val allCommits = mutableListOf<Commit>().apply {
                    var next: Int? = 1
                    while (next != null) {
                        val commits = gitFeed.commits(next)
                        addAll(commits.entries)
                        next = commits.paging.next
                    }
                }
                allCommits.size shouldBe 7
            }
        }
    }

    describe("extract paging") {

        it("should extract paging from page 2") {
            val node = obj {
                "pagelen" to 10
                "size" to 50
                "previous" to "http://foo.com?page=1"
                "next" to "http://foo.com?page=3"
            }.asTree()
            val paging = with(BitbucketGitFeed(mock())) {
                node.extractPagingBB()
            }
            paging shouldBe Paging(1, 1, 3, 5)
        }
        it("should extract paging from last page") {
            val node = obj {
                "pagelen" to 10
                "size" to 50
                "previous" to "http://foo.com?page=2"
            }.asTree()
            val paging = with(BitbucketGitFeed(mock())) {
                node.extractPagingBB()
            }
            paging shouldBe Paging(1, 2, null, null)
        }
        it("should extract paging from first page") {
            val node = obj {
                "pagelen" to 10
                "size" to 50
                "next" to "http://foo.com?page=2"
            }.asTree()
            val paging = with(BitbucketGitFeed(mock())) {
                node.extractPagingBB()
            }
            paging shouldBe Paging(null, null, 2, 5)
        }
        it("should extract paging from 1 page all-around") {
            val node = obj {
                "pagelen" to 10
                "size" to 10
            }.asTree()
            val paging = with(BitbucketGitFeed(mock())) {
                node.extractPagingBB()
            }
            paging shouldBe Paging(null, null, null, null)
        }
    }
})
