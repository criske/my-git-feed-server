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

import pcf.crskdev.gitfeed.server.core.feed.models.Assignment
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import pcf.crskdev.gitfeed.server.core.getOrThrowGitFeedException
import pcf.crskdev.inval.id.Rules
import pcf.crskdev.inval.id.validates
import pcf.crskdev.inval.id.withId

/**
 * Git feed that validates inputs before sending to delegate.
 *
 * @property delegate Delegate.
 * @author Cristian Pela
 */
class ValidGitFeed(private val delegate: GitFeed) : GitFeed by delegate {

    override fun commits(page: Int?): Commits =
        this.pageCheckDelegation(page, "Commits") {
            delegate.commits(page)
        }

    override fun assignments(state: Assignment.State, page: Int?): Assignments =
        this.pageCheckDelegation(page, "Assignments") {
            delegate.assignments(state, page)
        }

    override fun repos(page: Int?): Repos =
        this.pageCheckDelegation(page, "Repositories") {
            delegate.repos(page)
        }

    override fun equals(other: Any?): Boolean {
        return this.delegate == other
    }

    override fun hashCode(): Int {
        return this.delegate.hashCode()
    }

    /**
     * Checks if page is valid and the delegate.
     *
     * @param T Type.
     * @param page Page.
     * @param prefixMessage Starting message.
     * @param delegation Call delegation.
     * @receiver Nothing
     * @return T
     */
    private fun <T> pageCheckDelegation(page: Int?, prefixMessage: String, delegation: () -> T): T =
        if (page != null) {
            (Rules.Positive { "$prefixMessage page number must be positive" } validates page withId "page")()
                .map { delegation() }
                .getOrThrowGitFeedException()
        } else {
            delegation()
        }
}
