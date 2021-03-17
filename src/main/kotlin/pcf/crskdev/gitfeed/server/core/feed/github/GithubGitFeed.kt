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

import pcf.crskdev.gitfeed.server.core.feed.GitFeed
import pcf.crskdev.gitfeed.server.core.feed.extractPaging
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.headers
import pcf.crskdev.gitfeed.server.core.net.request
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

    override fun commits(page: Int?): Commits {
        val pageParam = if (page != null) "&page=$page" else ""
        return this.client.request(
            URI.create("$baseUrl/search/commits?q=author:criske&sort=author-date$pageParam"),
            headers { "Accept" to "application/vnd.github.cloak-preview+json" }
        ) {
            obj {
                "paging" to it.headers.extractPaging()
                "entries" to arr {
                    it.body["items"].elements().forEach {
                        +obj {
                            "sha" to it["sha"].asText().substring(0, 7)
                            "date" to it["commit"]["author"]["date"].asText()
                            "url" to it["html_url"].asText()
                            "message" to it["commit"]["message"].asText()
                            "repo" to obj {
                                "fullName" to it["repository"]["full_name"].asText()
                                "url" to it["repository"]["html_url"].asText()
                                "owner" to obj {
                                    "name" to it["repository"]["owner"]["login"].asText()
                                    "avatar" to it["repository"]["owner"]["avatar_url"].asText()
                                    "url" to it["repository"]["owner"]["html_url"].asText()
                                }
                            }
                        }
                    }
                }
            }.asTree()
        }
    }
}
