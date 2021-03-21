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

import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import redis.clients.jedis.JedisPool
import java.util.concurrent.atomic.AtomicReference

/**
 * Redis client.
 *
 */
object RedisClient {

    private val factory: () -> CacheStore = {
        object : CacheStore {

            /**
             * Cluster
             */
            private val client = JedisPool(
                RedisInfo.HOST,
                RedisInfo.PORT
            ).resource

            override fun set(key: String, value: String) {
                client.set(key, value)
            }

            override fun get(key: String): String? = client[key]

            override fun exists(key: String): Boolean = client.exists(key)

            override fun close() {
                client.close()
            }
        }
    }

    /**
     * Current cache.
     */
    private val cache = AtomicReference(factory())

    /**
     * Invokes the [CacheStore] for redis basic commands
     *
     * @param new Creates a new client instance on invoke,
     * @return CacheStore.
     */
    operator fun invoke(new: Boolean = false): CacheStore {
        return if (new) {
            cache.updateAndGet {
                it.close()
                factory()
            }
        } else {
            cache.get()
        }
    }
}
