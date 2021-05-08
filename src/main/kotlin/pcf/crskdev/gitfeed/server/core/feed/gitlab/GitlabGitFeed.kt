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

package pcf.crskdev.gitfeed.server.core.feed.gitlab

import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.extractPaging
import pcf.crskdev.gitfeed.server.core.feed.models.Assignments
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.RepoExtended
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import pcf.crskdev.gitfeed.server.core.feed.models.User
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.request
import pcf.crskdev.gitfeed.server.core.util.obj
import java.net.URI

/**
 * Gitlab git feed implementation with an optional configuration.
 *
 * Supported configuration keys:
 * - commitsPageSize: Int (default 30)
 *
 * @property client Authenticated client
 * @property config Configuration.
 * @author Cristian Pela
 */
class GitlabGitFeed(
    private val client: RequestClient,
    private val config: Map<String, String> = mapOf(COMMITS_PAGE_SIZE to "30")
) : GitFeed {

    companion object {
        const val COMMITS_PAGE_SIZE = "commitsPageSize"
    }

    /**
     * Base url.
     */
    private val baseUrl = "https://gitlab.com/api/v4"

    /**
     * User id.
     */
    private val userId = "6018288"

    override fun commits(page: Int?): Commits {
        TODO("Not yet implemented")
    }

    override fun assignments(state: Assignments.State, page: Int?): Assignments {
        TODO("Not yet implemented")
    }

    override fun me(): User = this.client
        .request(URI.create("$baseUrl/user")) {
            val node = it.body
            obj {
                "name" to node["username"]
                "avatar" to node["avatar_url"]
                "url" to node["web_url"]
                "type" to (node["type"] ?: "User")
                "provider" to "Gitlab"
            }.asTree()
        }

    override fun repos(page: Int?): Repos = this.repos(page) { !it.isFork }

    /**
     * Filtered repos.
     *
     * @param page Page number.
     * @param filter Predicate
     * @receiver Takes RepoExtended and returns Boolean.
     * @return Repos.
     */
    private inline fun repos(page: Int?, filter: (RepoExtended) -> Boolean): Repos {
        return this.client
            .request<Repos>(URI.create("$baseUrl/users/$userId/projects?visibility=public&per_page=100&page=${page ?: 1}")) {
                obj {
                    "paging" to it.headers.extractPaging()
                    "entries" to arr {
                        it.body.elements().forEach {
                            +obj {
                                "simple" to obj {
                                    "name" to it["name"]
                                    "fullName" to it["path_with_namespace"]
                                    "url" to it["web_url"]
                                    "owner" to obj {
                                        "name" to it["owner"]["username"]
                                        "avatar" to it["owner"]["avatar_url"]
                                        "url" to it["owner"]["web_url"]
                                        "type" to (it["owner"]["type"] ?: "User")
                                        "provider" to "Gitlab"
                                    }
                                }
                                "description" to it["description"]
                                "isFork" to (it["forked_from_project"] != null)
                                "isPrivate" to (it["visibility"].asText() != "public")
                                "stars" to it["star_count"]
                                "createdAt" to it["created_at"]
                                "updatedAt" to it["last_activity_at"]
                            }
                        }
                    }
                }.asTree()
            }.run {
                this.copy(entries = this.entries.filter(filter))
            }
    }
}
