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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializable
import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.extractPaging
import pcf.crskdev.gitfeed.server.core.feed.models.Assignment
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.RepoExtended
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.headers
import pcf.crskdev.gitfeed.server.core.net.request
import pcf.crskdev.gitfeed.server.core.util.ObjectScope
import pcf.crskdev.gitfeed.server.core.util.obj
import java.net.URI

/**
 * Github git feed implementation.
 *
 * @property client Authenticated client
 * @author Cristian Pela
 */
class GithubGitFeed(private val client: RequestClient) : GitFeed {

    /**
     * Base url.
     */
    private val baseUrl = "https://api.github.com"

    /**
     * Api header.
     */
    private val apiHeader = headers {
        "Accept" to "application/vnd.github.v3+json"
    }

    /**
     * Search feature header.
     */
    private val searchHeader = headers(apiHeader) {
        "Accept" to "application/vnd.github.cloak-preview+json"
    }

    override fun commits(page: Int?): Commits = this.client.request(
        this.uriWithPage("/search/commits?q=author:criske&sort=author-date", page),
        this.searchHeader
    ) {
        obj {
            "paging" to it.headers.extractPaging()
            "entries" to arr {
                it.body["items"].elements().forEach {
                    +obj {
                        "sha" to it["sha"].asText().substring(0, 7)
                        "date" to it["commit"]["author"]["date"]
                        "url" to it["html_url"]
                        "message" to it["commit"]["message"]
                        "repo" to simpleRepo(it["repository"])
                    }
                }
            }
        }.asTree()
    }

    override fun assignments(state: Assignment.State, page: Int?): Assignments {
        val stateQuery = when (state) {
            Assignment.State.ALL -> ""
            Assignment.State.CLOSED -> "+state:closed"
            Assignment.State.OPEN -> "+state:open"
        }
        val fastClient = this.client.fastCache()
        return fastClient.request(
            this.uriWithPage("search/issues?q=assignee:criske$stateQuery", page),
            this.searchHeader
        ) {
            obj {
                "paging" to it.headers.extractPaging()
                "entries" to arr {
                    it.body["items"].elements().forEach {
                        +obj {
                            "title" to it["title"]
                            "body" to it["body"]
                            "url" to it["html_url"]
                            "isOpen" to (it["state"].asText() != "closed")
                            "repo" to fastClient.fetchRepo(it["repository_url"].asText()).simple
                            "author" to user(it["user"])
                        }
                    }
                }
            }.asTree()
        }
    }

    override fun me(): User = this.client
        .request(URI.create("$baseUrl/users/criske"), this.apiHeader) { user(it.body) }

    override fun repos(page: Int?): Repos =
        this.client.request(
            this.uriWithPage("search/repositories?q=user:criske+fork:false&sort=updated", page),
            this.searchHeader
        ) {
            obj {
                "paging" to it.headers.extractPaging()
                "entries" to arr {
                    it.body["items"].elements().forEach {
                        +obj { applyRepo(it) }
                    }
                }
            }.asTree()
        }

    /**
     * Uri with optional page query param.
     *
     * @param path Path.
     * @param page Int.
     * @return URI
     */
    private fun uriWithPage(path: String, page: Int?): URI {
        val pageParam = if (page != null) "&page=$page" else ""
        val slash = if (path.startsWith("/")) "" else "/"
        return URI.create("$baseUrl$slash$path$pageParam")
    }

    /**
     * Get repo from remote.
     *
     * @param url Url
     * @return RepoExtended.
     */
    private fun RequestClient.fetchRepo(url: String): RepoExtended =
        this.request(URI.create(url)) {
            obj {
                applyRepo(it.body)
            }.asTree()
        }

    /**
     * Apply repo
     *
     * @param node
     */
    private fun ObjectScope.applyRepo(node: JsonNode) = apply {
        "simple" to simpleRepo(node)
        "description" to node["description"]
        "isFork" to node["fork"]
        "isPrivate" to node["private"]
        "stars" to node["stargazers_count"]
        "language" to node["language"]
        "organization" to node["organization"]?.let { user(it) }
        "createdAt" to node["created_at"]
        "updatedAt" to node["updated_at"]
    }

    /**
     *  Apply extracted repo data from a github json response key to ObjectScope.
     *
     * @param node JsonNode
     */
    private fun ObjectScope.simpleRepo(node: JsonNode): JsonSerializable = obj {
        "name" to node["name"]
        "fullName" to node["full_name"]
        "url" to node["html_url"]
        "owner" to user(node["owner"])
    }

    /**
     * Extract user from a github json response key.
     *
     * @return JsonNode
     */
    private fun ObjectScope.user(node: JsonNode) = obj {
        applyUser(this, node)
    }

    /**
     * Extract user from a github json response key.
     *
     * @return JsonNode
     */
    private fun user(node: JsonNode) = obj {
        applyUser(this, node)
    }.asTree()

    /**
     * Apply extracted user data from a github json response key to ObjectScope.
     *
     * @param objectScope ObjectScope.
     * @param node JsonNode.
     */
    private fun applyUser(objectScope: ObjectScope, node: JsonNode) =
        objectScope.apply {
            "name" to node["login"]
            "avatar" to node["avatar_url"]
            "url" to node["html_url"]
            "type" to node["type"]
            "provider" to "Github"
        }
}
