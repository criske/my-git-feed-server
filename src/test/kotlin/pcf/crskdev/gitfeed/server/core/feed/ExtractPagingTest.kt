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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.net.headers

internal class ExtractPagingTest : StringSpec({

    "should extract paging from header github" {
        val paging = headers {
            "Link" to """<https://api.github.com/search/commits?q=author%3Acriske&sort=author-date&page=2>; rel="next", <https://api.github.com/search/commits?q=author%3Acriske&sort=author-date&page=34>; rel="last""""
        }.extractPaging().toString()
        paging shouldBe """{"first":null,"prev":null,"next":2,"last":34}"""
    }

    "should extract paging from header gitlab" {
        val paging = headers {
            "Link" to "<https://gitlab.com/api/v4/projects/criske%2Fself-rest/repository/commits?id=criske%2Fself-rest&order=default&page=2&per_page=1>; rel=\"next\", <https://gitlab.com/api/v4/projects/criske%2Fself-rest/repository/commits?id=criske%2Fself-rest&order=default&page=1&per_page=1>; rel=\"first\", <https://gitlab.com/api/v4/projects/criske%2Fself-rest/repository/commits?id=criske%2Fself-rest&order=default&page=2&per_page=1>; rel=\"last\""
        }.extractPaging().toString()
        paging shouldBe """{"first":1,"prev":null,"next":2,"last":2}"""
    }
})
