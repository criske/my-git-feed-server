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

import pcf.crskdev.gitfeed.server.core.util.base64Decode
import pcf.crskdev.gitfeed.server.core.util.base64Encode

/**
 * Cache Keys encoder, decoder.
 *
 * Keys are in format {prefix}SEPARATOR{value}.
 *
 * @author Cristian Pela
 */
object CacheKeys {

    /**
     * S e p a r a t o r
     */
    internal const val SEPARATOR = "[$---$]"

    /**
     * Concrete Key Type used by domain.
     *
     * @property prefix Prefix stored on cache.
     * @constructor Create empty Type
     */
    enum class Type(val prefix: String) {
        /**
         * Etag key.
         *
         */
        ETAG("etag"),

        /**
         * Cached response data key
         *
         */
        RESPONSE("res"),

        /**
         * Creation timestamp of cached response.
         *
         */
        TIME("time")
    }

    /**
     * Creates a raw key based on a [CacheKey]. The key is base64 encoded.
     *
     * @param key [CacheKey].
     * @return Raw string key.
     */
    fun create(key: CacheKey): String = base64Encode(
        key.first.prefix,
        key.second,
        separator = SEPARATOR,
        padded = false
    )

    /**
     * Try to get a [CacheKey] from raw encoded key.
     *
     * @param key Raw encoded string key.
     * @return CachedKey.
     * @throws IllegalStateException if key could not be decoded.
     * @throws IllegalArgumentException if key is not matched with
     * the provided concrete key types [CacheKeys.Type]
     */
    fun getFromRaw(key: String): CacheKey = base64Decode(key, false).let { decoded ->
        val (rawType, value) = decoded
            .split(SEPARATOR)
            .takeIf { it.size == 2 }
            ?: throw IllegalStateException("Invalid key. Must follow this format {prefix}$SEPARATOR{value}. Current: $decoded")
        val type = Type.valueOf(rawType.toUpperCase())
        type to value
    }
}

typealias CacheKey = Pair<CacheKeys.Type, String>

fun CacheKeys.Type.createKey(value: String) = this to value

fun CacheKey.switch(prefix: CacheKeys.Type) = if(this.first != prefix)
    prefix to this.second else this
fun CacheKey.raw() = CacheKeys.create(this)
