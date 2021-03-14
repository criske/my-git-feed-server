package pcf.crskdev.gitfeed.server.util.cache

import pcf.crskdev.gitfeed.server.core.cache.CacheStore

/**
 * In memory cache store.
 *
 */
class InMemoryCacheStore : CacheStore {

    override fun set(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun get(key: String): String = "In memory cache works"

    override fun exists(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        // no-op
    }
}
