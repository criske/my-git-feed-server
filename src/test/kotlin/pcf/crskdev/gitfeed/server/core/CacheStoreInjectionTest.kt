package pcf.crskdev.gitfeed.server.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.test.context.ContextConfiguration
import pcf.crskdev.gitfeed.server.util.spring.TestBeanFactories

@ContextConfiguration(classes = [TestBeanFactories::class, CacheStoreInjection::class])
internal class CacheStoreInjectionTest(cacheComponent: CacheStoreInjection) : StringSpec() {
    init {
        "should call in memory cache" {
            cacheComponent.value("foo") shouldBe "In memory cache works"
        }
    }
}
