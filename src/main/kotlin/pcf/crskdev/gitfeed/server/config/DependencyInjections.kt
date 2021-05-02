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

package pcf.crskdev.gitfeed.server.config

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import pcf.crskdev.gitfeed.server.core.cache.CacheStore
import pcf.crskdev.gitfeed.server.core.feed.GitFeedManager
import pcf.crskdev.gitfeed.server.core.feed.GitFeedManagerImpl
import pcf.crskdev.gitfeed.server.core.net.RequestClient
import pcf.crskdev.gitfeed.server.core.net.RequestClientImpl
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.RequestCommandGraceDurationProxy
import pcf.crskdev.gitfeed.server.impl.core.cache.EhcacheStore
import pcf.crskdev.gitfeed.server.impl.core.cache.NaiveInMemoryCacheStore
import pcf.crskdev.gitfeed.server.impl.core.net.RestTemplateCommand

/**
 * Place for dependency injections.
 *
 */
@Configuration
class DependencyInjections {

    /**
     * Cache store bean.
     *
     * @return CacheStore.
     */
    @Bean
    @Profile("prod")
    fun cacheStore(manager: CacheManager): CacheStore = EhcacheStore(manager)

    /**
     * Cache store bean.
     *
     * @return CacheStore.
     */
    @Bean
    @Profile("dev")
    fun cacheStoreDev(): CacheStore = NaiveInMemoryCacheStore()

    /**
     * Request command.
     *
     * @return RequestCommand
     */
    @Bean
    fun requestCommand(): RequestCommand = RestTemplateCommand()

    /**
     * Request command grace duration proxy
     *
     * @param cache
     * @param requestCommand
     * @return
     */
    @Bean
    fun requestCommandGraceDurationProxy(
        cache: CacheStore,
        requestCommand: RequestCommand
    ): RequestCommandGraceDurationProxy = RequestCommandGraceDurationProxy(cache, requestCommand)

    /**
     * Request client
     *
     * @param requestCommandProxy Grace duration command request proxy.
     * @return RequestClient
     */
    @Bean
    fun requestClient(requestCommandProxy: RequestCommandGraceDurationProxy): RequestClient =
        RequestClientImpl(requestCommandProxy, requestCommandProxy)

    /**
     * Git feed manager.
     *
     * @param client RequestClient.
     * @return GitFeedManager.
     */
    @Bean
    fun gitFeedManager(client: RequestClient): GitFeedManager = GitFeedManagerImpl(client)
}
