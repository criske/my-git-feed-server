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

import com.fasterxml.jackson.databind.JsonNode
import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.request
import pcf.crskdev.gitfeed.server.core.util.ObjectScope
import pcf.crskdev.gitfeed.server.core.util.obj
import java.net.URI

/**
 * Bitbucket git feed implementation.
 *
 * @property client Authenticated client
 * @author Cristian Pela
 */
class BitbucketGitFeed(private val client: RequestClient) : GitFeed {

    /**
     * Base url.
     */
    private val baseUrl = "https://bitbucket.org/api/2.0"

    override fun commits(page: Int?): Commits {
        TODO("Not yet implemented")
    }

    override fun assignments(state: Assignments.State, page: Int?): Assignments {
        TODO("Not yet implemented")
    }

    override fun me(): User = this.client
        .request(URI.create("$baseUrl/user")) { user(it.body) }

    override fun repos(page: Int?): Repos {
        TODO("Not yet implemented")
    }

    /**
     * Extract user from a bitbucket json response key.
     *
     * @return JsonNode
     */
    private fun ObjectScope.user(node: JsonNode) = obj {
        applyUser(this, node)
    }

    /**
     * Extract user from a bitbucket json response key.
     *
     * @return JsonNode
     */
    private fun user(node: JsonNode) = obj {
        applyUser(this, node)
    }.asTree()

    /**
     * Apply extracted user data from a bitbucket json response key to ObjectScope.
     *
     * @param objectScope ObjectScope.
     * @param node JsonNode.
     */
    private fun applyUser(objectScope: ObjectScope, node: JsonNode) =
        objectScope.apply {
            "name" to node["username"]
            "avatar" to node["links"]["avatar"]["href"]
            "url" to node["links"]["html"]["href"]
            "type" to node["type"]
            "provider" to "Bitbucket"
        }
}
