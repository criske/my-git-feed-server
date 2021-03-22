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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.util.KLogger
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.net.URI
import java.util.concurrent.atomic.AtomicReference

/**
 * Redis client.
 * TODO: remove redis dependencies.
 *
 */
@Deprecated("Ehcache is used now")
object RedisClient {

    private val factory: () -> CacheStore = {
        object : CacheStore {

            /**
             * Logger.
             */
            private val logger = KLogger<RedisClient>()

            /**
             * Pool
             * configuration based on this [article](https://partners-intl.aliyun.com/help/doc-detail/98726.htm)
             */
            private val pool = URI.create(RedisInfo.URL).let { uri ->
                JedisPool(
                    GenericObjectPoolConfig<Jedis>().apply {
                        maxTotal = 8
                        minIdle = 0
                        maxIdle = 8
                        blockWhenExhausted = true
                        maxWaitMillis = 10000
                        testOnBorrow = true
                        testWhileIdle = true
                    },
                    uri.host,
                    uri.port
                )
            }

            override fun set(key: String, value: String) {
                pool.resource
                    .use { jedis -> jedis.set(key, value) }
                    .runCatching { this }
                    .onFailure { logger.error { it.message } }
                    .getOrDefault(Unit)
            }

            override fun get(key: String): String? = pool.resource
                .use { jedis -> jedis.get(key) }
                .runCatching { this }
                .onFailure { logger.error { it.message } }
                .getOrNull()

            override fun exists(key: String): Boolean = pool.resource
                .use { jedis -> jedis.exists(key) }
                .runCatching { this }
                .onFailure { logger.error { it.message } }
                .getOrDefault(false)

            override fun close() {
                pool.close()
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
