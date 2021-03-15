package pcf.crskdev.gitfeed.server.util.spring

import com.nhaarman.mockitokotlin2.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.RequestCommand

@TestConfiguration
class TestBeanFactories {

    /**
     * Cache store bean.
     *
     * @return CacheStore.
     */
    @Bean
    fun cacheStore(): CacheStore = mock()

    /**
     * RequestCommand bean.
     *
     * @return RequestCommand.
     */
    @Bean
    fun requestCommand(): RequestCommand = mock()

    /**
     * Request client.
     *
     * @return RequestClient.
     */
    @Bean
    @Scope("prototype")
    fun requestClient(cacheStore: CacheStore, requestCommand: RequestCommand): RequestClient =
        RequestClient(cacheStore, requestCommand)
}
