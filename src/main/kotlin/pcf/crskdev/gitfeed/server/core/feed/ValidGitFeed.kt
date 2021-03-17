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
import pcf.crskdev.gitfeed.server.core.GitFeedException.Type
import pcf.crskdev.gitfeed.server.core.feed.models.Commits
import pcf.crskdev.gitfeed.server.core.util.obj
import pcf.crskdev.inval.id.Rules
import pcf.crskdev.inval.id.ValidationException
import pcf.crskdev.inval.id.validates
import pcf.crskdev.inval.id.withId

/**
 * Git feed that validates inputs before sending to delegate.
 *
 * @property delegate Delegate.
 * @author Cristian Pela
 */
class ValidGitFeed(private val delegate: GitFeed) : GitFeed {

    override fun commits(page: Int?): Commits =
        if (page != null) {
            (Rules.Positive { "Commits page number must be positive" } validates page withId "page")()
                .map { delegate.commits(it.toInt()) }
                .getOrThrowGitFeedException()
        } else {
            delegate.commits(page)
        }

    override fun equals(other: Any?): Boolean {
        return this.delegate == other
    }

    override fun hashCode(): Int {
        return this.delegate.hashCode()
    }

    /**
     * Get result T or throw a ValidationException converted to GitFeedException.
     *
     * @param T value type.
     * @return value
     * @throws GitFeedException
     */
    private fun <T> Result<T>.getOrThrowGitFeedException(): T =
        try {
            this.getOrThrow()
        } catch (ex: Throwable) {
            throw when (ex) {
                is ValidationException -> {
                    val json = obj {
                        "violations" to arr {
                            ex.violations.forEach { violation ->
                                +obj {
                                    violation.id.toString() to violation.message
                                }
                            }
                        }
                    }
                    GitFeedException(type = Type.VALIDATION, json.asTree())
                }
                is GitFeedException -> ex
                else -> GitFeedException.UNKNOWN
            }
        }
}
