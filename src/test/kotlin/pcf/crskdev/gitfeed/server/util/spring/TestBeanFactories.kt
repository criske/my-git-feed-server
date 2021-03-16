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
