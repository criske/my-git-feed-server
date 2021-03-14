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

            override fun get(key: String): String = client[key]

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
