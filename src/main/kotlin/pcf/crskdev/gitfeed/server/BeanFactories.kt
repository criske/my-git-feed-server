package pcf.crskdev.gitfeed.server

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.impl.core.cache.RedisClient

/**
 * Place for dependency injections.
 *
 */
@Configuration
class BeanFactories {

    /**
     * Cache store bean.
     *
     * @return CacheStore.
     */
    @Bean
    fun cacheStore(): CacheStore = RedisClient()
}
