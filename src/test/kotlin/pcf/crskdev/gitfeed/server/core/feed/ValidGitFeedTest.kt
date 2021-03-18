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

package pcf.crskdev.gitfeed.server.core.feed

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Paging

internal class ValidGitFeedTest : DescribeSpec({

    describe("commits validations") {
        it("should pass validation") {
            val delegate = mock<GitFeed>()
            val gitFeed = ValidGitFeed(delegate)
            val result = Commits(Paging(), emptyList())

            whenever(delegate.commits(1)).thenReturn(result)
            gitFeed.commits(1) shouldBe result
        }

        it("should throw if page is not positive") {
            val delegate = mock<GitFeed>()
            val gitFeed = ValidGitFeed(delegate)
            val err = shouldThrow<GitFeedException> {
                gitFeed.commits(0)
            }
            err.message shouldBe """{"type":"validation","error":{"violations":[{"page":"Commits page number must be positive"}]}}"""
        }

        it("should skip validation if page is not set") {
            val delegate = mock<GitFeed>()
            val gitFeed = ValidGitFeed(delegate)
            gitFeed.commits()
            verify(delegate).commits(null)
        }

        it("should throw git feed exception when validation delegate throws git feed exception") {
            val exception = GitFeedException.fromString(GitFeedException.Type.HTTP)
            val delegate = mock<GitFeed>()
            val gitFeed = ValidGitFeed(delegate)

            whenever(delegate.commits(1)).thenThrow(exception)
            shouldThrow<GitFeedException> {
                gitFeed.commits(1)
            } shouldBe exception
        }
    }

    describe("assignments validation") {
        it("should pass validation") {
            val delegate = mock<GitFeed>()
            val gitFeed = ValidGitFeed(delegate)
            val result = Assignments(Paging(), emptyList())

            whenever(delegate.assignments(page = 1)).thenReturn(result)
            gitFeed.assignments(page = 1) shouldBe result
        }

        it("should throw if page is not positive") {
            val delegate = mock<GitFeed>()
            val gitFeed = ValidGitFeed(delegate)
            val err = shouldThrow<GitFeedException> {
                gitFeed.assignments(page = 0)
            }
            err.message shouldBe """{"type":"validation","error":{"violations":[{"page":"Assignments page number must be positive"}]}}"""
        }
    }
})
