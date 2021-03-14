package pcf.crskdev.gitfeed.server.impl.core.cache

/**
 * Redis info (host and port).
 *
 */
object RedisInfo {
    /**
     * H o s t
     */
    val HOST: String = System.getenv("redis_host")
        ?: throw IllegalStateException("`redis_host` env variable is not set")

    /**
     * P o r t
     */
    val PORT: Int = System.getenv("redis_port")?.toInt()
        ?: throw IllegalStateException("`redis_port` env variable is not set")

    /**
     * U s e r
     */
    val USER: String = System.getenv("redis_user") ?: "default"

    /**
     * P a s s w o r d. May be null.
     */
    val PASSWORD: String? = System.getenv("redis_password")
}
