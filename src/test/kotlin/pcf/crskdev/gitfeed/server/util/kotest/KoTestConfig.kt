package pcf.crskdev.gitfeed.server.util.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.spring.SpringListener

/**
 * KoTest configuration class.
 *
 */
class KoTestConfig : AbstractProjectConfig() {
    override fun listeners() = listOf(SpringListener)
}
