package pcf.crskdev.gitfeed.server.util.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension
import io.kotest.extensions.spring.testContextManager

/**
 * KoTest configuration class.
 *
 */
class KoTestConfig : AbstractProjectConfig() {
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}

suspend inline fun <reified T> getBean(): T = testContextManager().testContext.applicationContext.getBean(T::class.java)
