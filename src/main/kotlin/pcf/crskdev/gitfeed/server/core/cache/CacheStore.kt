package pcf.crskdev.gitfeed.server.core.cache

import java.io.Closeable

/**
 * Cache store.
 *
 */
interface CacheStore : Closeable {

    /**
     * Store the key value pair on cache.
     *
     * @param key String
     * @param value String
     */
    operator fun set(key: String, value: String)

    /**
     * Get a value of a key.
     *
     * @param key String
     * @return String
     */
    operator fun get(key: String): String?

    /**
     * Checks if key exists.
     *
     * @param key String.
     * @return Boolean.
     */
    fun exists(key: String): Boolean
}
