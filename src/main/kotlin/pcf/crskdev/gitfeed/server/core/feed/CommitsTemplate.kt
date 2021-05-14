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

import pcf.crskdev.gitfeed.server.core.feed.models.Commit
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.feed.models.Paging
import pcf.crskdev.gitfeed.server.core.feed.models.Repo
import pcf.crskdev.gitfeed.server.core.feed.models.Repos
import java.time.OffsetDateTime
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.min

/**
 * Commits template that can be applied for those [GitFeed] providers
 * that don't offer a feature to get all commits from all repositories.
 *
 * The algorithm:
 * - fetch all repos that the auth user has contributed.
 * - for each repo fetch all commits by latest.
 * - merge them into a [Commits] data structure.
 *
 *
 * @property commitsPageSize Page size
 * @property allRepos Lambda that takes current page and return Repos for that.
 * @property commitsOfOneRepo Lambda tha takes current repo and page and returns
 * its commits.
 * @author Cristian Pela
 */
class CommitsTemplate(
    private val commitsPageSize: Int,
    private val allRepos: (Int) -> Repos,
    private val commitsOfOneRepo: (Repo, Int) -> Commits
) {

    fun execute(page: Int?): Commits {
        val repos = mutableListOf<Repo>().apply {
            var next: Int? = 1
            while (next != null) {
                allRepos(next).also { r ->
                    addAll(r.entries.map { it.simple })
                    next = r.paging.next
                }
            }
        }

        val allCommits: List<Commit> = repos
            .parallelStream()
            .flatMap { repo ->
                mutableListOf<Commit>()
                    .apply {
                        var next: Int? = 1
                        while (next != null) {
                            val pageCommits = commitsOfOneRepo(repo, next!!)
                            addAll(pageCommits.entries)
                            next = pageCommits.paging.next
                        }
                    }.toList().stream()
            }
            .collect(Collectors.toList())
            .sortedWith { a, b ->
                // order descending
                -1 * OffsetDateTime.parse(a.date)
                    .compareTo(OffsetDateTime.parse(b.date))
            }

        val pageSize = this.commitsPageSize
        val lastPage = ceil(allCommits.size.toDouble().div(pageSize)).toInt()
        val currPage = page ?: 1
        return Commits(
            Paging(
                if (currPage == 1) null else 1,
                if (currPage > 1) currPage - 1 else null,
                if (currPage < lastPage) currPage + 1 else null,
                if (currPage == lastPage) null else lastPage
            ),
            allCommits.subList(
                (currPage - 1) * pageSize,
                min((currPage - 1) * pageSize + pageSize, allCommits.size)
            )
        )
    }
}
