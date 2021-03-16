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

package pcf.crskdev.gitfeed.server.impl.core.cache

/**
 * Redis info (host and port).
 *
 */
object RedisInfo {
    /**
     * H o s t
     */
    val HOST: String = System.getenv("redis_host")
        ?: throw IllegalStateException("`redis_host` env variable is not set")

    /**
     * P o r t
     */
    val PORT: Int = System.getenv("redis_port")?.toInt()
        ?: throw IllegalStateException("`redis_port` env variable is not set")

    /**
     * U s e r
     */
    val USER: String = System.getenv("redis_user") ?: "default"

    /**
     * P a s s w o r d. May be null.
     */
    val PASSWORD: String? = System.getenv("redis_password")
}
