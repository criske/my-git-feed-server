package pcf.crskdev.gitfeed.server.core

import org.springframework.stereotype.Component
import pcf.crskdev.gitfeed.server.core.cache.CacheStore

/**
 * TODO remove this only used to see if CacheStore is injected.
 */
@Component
class CacheStoreInjection(private val cache: CacheStore) {

    fun value(key: String): String = this.cache[key]
}
