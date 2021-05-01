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

package pcf.crskdev.gitfeed.server.core.cache

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.core.util.base64Encode

class CacheKeysTest : StringSpec({

    "should encode/decode key" {
        val key = CacheKeys.Type.ETAG to "https://bitbucket.org/api/2.0/repositories/cristianpela/dia-link-capstone-web-mobile/commits?pagelen=100&page=1"
        val encoded = CacheKeys.create(key)
        val decoded = CacheKeys.getFromRaw(encoded)
        decoded shouldBe key
    }
    "should create raw key with extension" {
        val raw = CacheKeys.Type.ETAG.createKey("123").raw()
        raw shouldBe CacheKeys.create(CacheKeys.Type.ETAG to "123")
    }

    "should switch prefix" {
        val key = CacheKeys.Type.ETAG to "123"
        val switched = key.switch(CacheKeys.Type.TIME)
        switched shouldBe (CacheKeys.Type.TIME to "123")
    }

    "should throw if key not found" {
        shouldThrow<IllegalStateException> {
            CacheKeys.getFromRaw("")
        }
        shouldThrow<IllegalStateException> {
            CacheKeys.getFromRaw(base64Encode("a", "test", "test", separator = CacheKeys.SEPARATOR, padded = false))
        }
        shouldThrow<IllegalArgumentException> {
            CacheKeys.getFromRaw(base64Encode("a", "test", separator = CacheKeys.SEPARATOR, padded = false))
        }
    }
})
