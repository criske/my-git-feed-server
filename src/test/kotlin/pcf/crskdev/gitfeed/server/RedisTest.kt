package pcf.crskdev.gitfeed.server

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import pcf.crskdev.gitfeed.server.impl.core.cache.RedisClient
import pcf.crskdev.gitfeed.server.impl.core.cache.RedisInfo
import redis.embedded.RedisServer

class RedisTest : StringSpec({

    "embedded redis should work" {

        val server = RedisServer.builder()
            .port(RedisInfo.PORT)
            .build()
            .apply { start() }

        RedisClient(true).use { redis ->
            redis.exists("foo") shouldBe false
            redis["foo"] = "bar"
            redis["foo"] shouldBe "bar"
            redis
        }

        server.stop()
    }
})
