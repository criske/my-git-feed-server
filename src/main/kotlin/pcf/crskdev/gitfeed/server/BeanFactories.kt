package pcf.crskdev.gitfeed.server

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.impl.core.cache.RedisClient
import pcf.crskdev.gitfeed.server.impl.core.net.RestTemplateCommand

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

    /**
     * Request command.
     *
     * @return RequestCommand
     */
    @Bean
    fun requestCommand(): RequestCommand = RestTemplateCommand()

    /**
     * Request client
     *
     * @param cache CacheStore.
     * @return RequestClient
     */
    @Bean
    fun requestClient(cache: CacheStore, requestCommand: RequestCommand): RequestClient =
        RequestClient(cache, requestCommand)
}
