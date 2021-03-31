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

import pcf.crskdev.gitfeed.server.core.feed.github.GithubGitFeed
import pcf.crskdev.gitfeed.server.core.net.AccessToken
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import java.util.EnumMap

/**
 * Git feed manager.
 *
 * @author Cristian Pela
 */
interface GitFeedManager {
    /**
     * Get a GitFeed implementation based on provider name.
     *
     * @param name Provider name.
     * @return GitFeed or [GitFeed.Unknown]
     */
    fun of(name: String): GitFeed
}

/**
 * Git feed manager implementation.
 *
 * @property client RequestClient
 * @property factory GitFeedFactory
 * @author Cristian Pela
 */
class GitFeedManagerImpl(
    private val client: RequestClient,
    private val factory: GitFeedFactory
) : GitFeedManager {

    constructor(client: RequestClient) : this(
        client,
        GitFeedFactory(
            Provider.GITHUB to {
                GithubGitFeed(client.authorized(it))
            }
        )
    )

    override fun of(name: String): GitFeed {
        val provider = Provider.valueOfSafe(name)
        return factory[provider]
            ?.invoke(provider.accessToken)
            ?.let { ValidGitFeed(it) }
            ?: GitFeed.Unknown(name)
    }
}

typealias GitFeedFactory = Map<Provider, (AccessToken) -> GitFeed>

fun GitFeedFactory(vararg entries: Pair<Provider, (AccessToken) -> GitFeed>): GitFeedFactory =
    EnumMap<Provider, (AccessToken) -> GitFeed>(Provider::class.java).apply {
        entries.forEach {
            put(it.first, it.second)
        }
    }.toMap()
