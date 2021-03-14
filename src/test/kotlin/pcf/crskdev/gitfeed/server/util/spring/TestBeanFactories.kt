package pcf.crskdev.gitfeed.server.util.spring

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.util.cache.InMemoryCacheStore

@TestConfiguration
class TestBeanFactories {

    /**
     * Cache store bean.
     *
     * @return CacheStore.
     */
    @Bean
    fun cacheStore(): CacheStore = InMemoryCacheStore()
}
