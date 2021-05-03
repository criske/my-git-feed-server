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

@file:Suppress("unused")

package pcf.crskdev.gitfeed.server.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.GitFeedManager
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commit
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Paging
import pcf.crskdev.gitfeed.server.core.feed.models.Repo
import pcf.crskdev.gitfeed.server.core.feed.models.RepoExtended
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.util.jsonTo
import pcf.crskdev.gitfeed.server.util.kotest.getBean

@WebMvcTest(GitFeedApiController::class)
internal class GitFeedApiControllerTest @Autowired constructor(mockMvc: MockMvc) : DescribeSpec() {

    @TestConfiguration
    class Inject {
        @Bean
        fun gitFeedManager(): GitFeedManager = mock()
    }

    init {

        describe("github") {
            describe("commits endpoint") {

                it("should fetch commits") {
                    val feed = mock<GitFeed>()
                    val commits = Commits(
                        Paging(next = 2, last = 34),
                        listOf(
                            Commit(
                                "d4496c7",
                                "2021-03-13T15:58:06.000+02:00",
                                "https://github.com/self-xdsd/self-web/commit/d4496c7c50e6ae443263e5fe5c13b631ca2cbebd",
                                "#371 Allow Deactivation Of All PaymentMethods.",
                                Repo(
                                    "self-web",
                                    "self-xdsd/self-web",
                                    "https://github.com/self-xdsd/self-web",
                                    User(
                                        "self-xdsd",
                                        "https://avatars.githubusercontent.com/u/65442807?v=4",
                                        "https://github.com/self-xdsd",
                                        "organization",
                                        "Github"
                                    )
                                )
                            )
                        )
                    )

                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("github")).thenReturn(feed)
                    whenever(feed.commits(null)).thenReturn(commits)

                    mockMvc
                        .perform(get("/api/github/commits"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Commits>() shouldBe commits
                        }
                }
            }

            describe("assignments endpoint") {
                val manager = getBean<GitFeedManager>()
                val feed = mock<GitFeed>()

                whenever(manager.of(any())).thenReturn(feed)

                beforeTest { reset(feed) }

                it("should fetch all assignments implicitly") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.ALL)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch all assignments explicitly") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.ALL)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=all"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch all assignments if state is unknown") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.ALL)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=foo"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch all assignments explicitly with page") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.ALL, 1)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=all&page=1"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch closed assignments with page") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.CLOSED, 1)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=closed&page=1"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch closed assignments") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.CLOSED)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=closed"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch open assignments with page") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.OPEN, 1)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=oPen&page=1"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }

                it("should fetch open assignments") {
                    val assignments = Assignments(Paging(), emptyList())
                    whenever(feed.assignments(Assignments.State.OPEN)).thenReturn(assignments)

                    mockMvc
                        .perform(get("/api/github/assignments?state=open"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Assignments>() shouldBe assignments
                        }
                }
            }

            describe("me endpoint") {
                it("should get me") {
                    val me = User(
                        "criske",
                        "https://avatars.githubusercontent.com/u/10284893?v=4",
                        "https://github.com/criske",
                        "User",
                        "Github"
                    )
                    val feed = mock<GitFeed>()
                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("github")).thenReturn(feed)
                    whenever(feed.me()).thenReturn(me)

                    mockMvc
                        .perform(get("/api/github/me"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<User>() shouldBe me
                        }
                }
            }

            describe("repos endpoint") {
                it("should get repos") {
                    val repos = Repos(
                        Paging(),
                        listOf(
                            RepoExtended(
                                Repo(
                                    "self-web",
                                    "self-xdsd/self-web",
                                    "https://github.com/self-xdsd/self-web",
                                    User(
                                        "self-xdsd",
                                        "https://avatars.githubusercontent.com/u/65442807?v=4",
                                        "https://github.com/self-xdsd",
                                        "organization",
                                        "Github"
                                    )
                                ),
                                "Simple description",
                                false,
                                isPrivate = false,
                                0,
                                "Java",
                                null,
                                "now",
                                "now"
                            )
                        )
                    )

                    val feed = mock<GitFeed>()
                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("github")).thenReturn(feed)
                    whenever(feed.repos()).thenReturn(repos)

                    mockMvc
                        .perform(get("/api/github/repos"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Repos>() shouldBe repos
                        }
                }
            }
        }

        describe("bitbucket") {
            describe("me endpoint") {
                it("should get me") {
                    val me = User(
                        "cristianpela",
                        "https://avatar-management--avatars.us-west-2.prod.public.atl-paas.net/557058:0f30dbbe-e90b-4d4a-a005-6fc83820c8e7/30024e46-ecc6-4fc3-9275-c97e2a8b8418/128",
                        "https://bitbucket.org/%7Bc94b65af-2573-4c7a-93ad-da943c45ecaf%7D/",
                        "User",
                        "Bitbucket"
                    )
                    val feed = mock<GitFeed>()
                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("bitbucket")).thenReturn(feed)
                    whenever(feed.me()).thenReturn(me)

                    mockMvc
                        .perform(get("/api/bitbucket/me"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<User>() shouldBe me
                        }
                }
            }
            describe("repos endpoint") {
                it("should get repos") {
                    val repos = Repos(
                        Paging(),
                        listOf(
                            RepoExtended(
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
                        )
                    )

                    val feed = mock<GitFeed>()
                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("bitbucket")).thenReturn(feed)
                    whenever(feed.repos()).thenReturn(repos)

                    mockMvc
                        .perform(get("/api/bitbucket/repos"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Repos>() shouldBe repos
                        }
                }
            }
            describe("commits endpoint") {

                it("should fetch commits") {
                    val feed = mock<GitFeed>()
                    val commits = Commits(
                        Paging(next = 2, last = 34),
                        listOf(
                            Commit(
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
                        )
                    )

                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("bitbucket")).thenReturn(feed)
                    whenever(feed.commits(null)).thenReturn(commits)

                    mockMvc
                        .perform(get("/api/bitbucket/commits"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<Commits>() shouldBe commits
                        }
                }
            }
        }

        describe("gitlab") {
            describe("me endpoint") {
                it("should get me") {
                    val me = User(
                        "criske",
                        "https://secure.gravatar.com/avatar/d50208500191eb9b0d99ed29be6facd5?s=80&d=identicon",
                        "https://gitlab.com/criske",
                        "User",
                        "Gitlab"
                    )
                    val feed = mock<GitFeed>()
                    val manager = getBean<GitFeedManager>()
                    whenever(manager.of("gitlab")).thenReturn(feed)
                    whenever(feed.me()).thenReturn(me)

                    mockMvc
                        .perform(get("/api/gitlab/me"))
                        .andDo(print()).andExpect(status().isOk)
                        .andExpect { result ->
                            result.response.contentAsString.jsonTo<User>() shouldBe me
                        }
                }
            }
        }
    }
}
