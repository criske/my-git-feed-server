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

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pcf.crskdev.gitfeed.server.core.feed.GitFeedManager
import pcf.crskdev.gitfeed.server.core.feed.models.Assignment
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import pcf.crskdev.gitfeed.server.core.feed.models.User

/**
 * Git feed API.
 *
 * @property manager GitFeedManager
 */
@RestController
@RequestMapping("/api/feeds")
class GitFeedApiController(private val manager: GitFeedManager) {

    /**
     * Commits endpoint.
     *
     * @param provider Provider.
     * @param page Page number
     * @return Commits.
     */
    @GetMapping("/commits/{provider}")
    fun commits(
        @PathVariable provider: String,
        @RequestParam(required = false) page: Int?
    ): Commits {
        return this.manager.of(provider).commits(page)
    }

    /**
     * Assignments endpoint.
     *
     * @param provider Provider.
     * @param state State(all, closed, open) or null.
     * @param page Page number
     * @return Assignments.
     */
    @GetMapping("/assignments/{provider}")
    fun assignments(
        @PathVariable provider: String,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) page: Int?
    ): Assignments {
        return this.manager.of(provider)
            .assignments(Assignment.State.valueOfSafe(state), page)
    }

    /**
     * Me endpoint.
     *
     * @param provider Provider.
     * @return User.
     */
    @GetMapping("/me/{provider}")
    fun me(@PathVariable provider: String): User = this.manager.of(provider).me()

    /**
     * Repos non-forked endpoint.
     *
     * @param provider Provider
     * @param page Page number
     * @return Repos.
     */
    @GetMapping("/repos/{provider}")
    fun repos(
        @PathVariable provider: String,
        @RequestParam(required = false) page: Int?
    ): Repos = this.manager.of(provider).repos(page)
}
