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

package pcf.crskdev.gitfeed.server.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.GitFeedManager
import pcf.crskdev.gitfeed.server.core.feed.models.Commit
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Owner
import pcf.crskdev.gitfeed.server.core.feed.models.Paging
import pcf.crskdev.gitfeed.server.core.feed.models.Repo
import pcf.crskdev.gitfeed.server.core.util.KObjectMapper
import pcf.crskdev.gitfeed.server.util.kotest.getBean
import pcf.crskdev.gitfeed.server.util.spring.TestBeanFactories

@WebMvcTest(GitFeedApiController::class)
@ContextConfiguration(classes = [TestBeanFactories::class])
internal class GitFeedApiControllerTest @Autowired constructor(mockMvc: MockMvc) : StringSpec() {

    init {

        "should fetch commits" {
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
                            "self-xdsd/self-web",
                            "https://github.com/self-xdsd/self-web",
                            Owner(
                                "self-xdsd",
                                "https://avatars.githubusercontent.com/u/65442807?v=4",
                                "https://github.com/self-xdsd"
                            )
                        )
                    )
                )
            )

            val manager = getBean<GitFeedManager>()
            whenever(manager.of("github")).thenReturn(feed)
            whenever(feed.commits(null)).thenReturn(commits)

            mockMvc
                .perform(get("/api/feeds/commits/github"))
                .andDo(print()).andExpect(status().isOk)
                .andExpect { result ->
                    KObjectMapper().readValue<Commits>(result.response.contentAsString) shouldBe commits
                }
        }
    }
}
