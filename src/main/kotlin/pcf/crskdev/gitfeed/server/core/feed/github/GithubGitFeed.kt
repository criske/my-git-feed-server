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
import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.extractPaging
import pcf.crskdev.gitfeed.server.core.feed.models.Assignment
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.RepoExtended
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
     * Headers.
     */
    private val previewHeaders = headers {
        "Accept" to "application/vnd.github.cloak-preview+json"
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

    override fun commits(page: Int?): Commits = this.client.request(
        this.uriWithPage("/search/commits?q=author:criske&sort=author-date", page),
        this.previewHeaders
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
                        "repo" to obj {
                            "fullName" to it["repository"]["full_name"]
                            "url" to it["repository"]["html_url"]
                            "owner" to getUser(it["repository"]["owner"])
                        }
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
            this.previewHeaders
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
                            "repo" to getRepo(fastClient, it["repository_url"].asText()).simple
                            "author" to getUser(it["user"])
                        }
                    }
                }
            }.asTree()
        }
    }

    override fun me(): User = this.client
        .request(URI.create("$baseUrl/users/criske")) { getUser(it.body) }

    private fun getRepo(client: RequestClient, url: String): RepoExtended =
        client.request(URI.create(url)) {
            obj {
                "simple" to obj {
                    "fullName" to it.body["full_name"]
                    "url" to it.body["html_url"]
                    "owner" to getUser(it.body["owner"])
                }
                "description" to it.body["description"]
                "isFork" to it.body["fork"]
                "stars" to it.body["stargazers_count"]
                "language" to it.body["language"]
                "organization" to it.body["organization"]?.let { getUser(it) }
                "createdAt" to it.body["created_at"]
                "updatedAt" to it.body["updated_at"]
            }.asTree()
        }

    /**
     * Get user from a github json response key.
     *
     * @return JsonNode
     */
    private fun ObjectScope.getUser(node: JsonNode) = obj {
        "name" to node["login"]
        "avatar" to node["avatar_url"]
        "url" to node["html_url"]
        "type" to node["type"]
    }

    /**
     * Get user from a github json response key.
     *
     * @return JsonNode
     */
    private fun getUser(node: JsonNode) = obj {
        "name" to node["login"]
        "avatar" to node["avatar_url"]
        "url" to node["html_url"]
        "type" to node["type"]
    }.asTree()
}
