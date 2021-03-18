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

import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.feed.models.Assignment
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits

/**
 * Git feed interface for all supported git platform providers.
 *
 * @author Cristian Pela
 */
interface GitFeed {

    /**
     * Latest commits.
     *
     * @param page Of page.
     * @return Commits.
     */
    fun commits(page: Int? = null): Commits

    /**
     * Latest assignments.
     *
     * @param state State("closed","open","all")
     * @param page Of page.
     * @return Assignments.
     */
    fun assignments(state: Assignment.State = Assignment.State.ALL, page: Int? = null): Assignments

    /**
     * Unknown GitFeed.
     *
     * @author Cristian Pela
     */
    object Unknown : GitFeed {

        /**
         * Exception thrown by each method of this [GitFeed] implementation.
         */
        private val exception = GitFeedException.fromString(
            GitFeedException.Type.VALIDATION,
            "Unknown git feed platform provider"
        )

        override fun commits(page: Int?): Commits {
            throw exception
        }

        override fun assignments(state: Assignment.State, page: Int?): Assignments {
            throw exception
        }
    }
}
