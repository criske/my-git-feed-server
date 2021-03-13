package pcf.crskdev.gitfeed.server.util

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.spring.SpringListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.web.servlet.MockMvc

open class SpringFunSpec(body: FunSpec.() -> Unit = {}) : FunSpec(body) {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ctx: ApplicationContext

    override fun listeners(): List<TestListener> {
        return listOf(SpringListener)
    }
}

open class SpringStringSpec(body: StringSpec.() -> Unit = {}) : StringSpec(body) {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ctx: ApplicationContext

    override fun listeners(): List<TestListener> {
        return listOf(SpringListener)
    }
}

open class SpringDescribeSpec(body: DescribeSpec.() -> Unit = {}) : DescribeSpec(body) {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ctx: ApplicationContext

    override fun listeners(): List<TestListener> {
        return listOf(SpringListener)
    }
}

val FunSpec.mockMvc
    get() = when (this) {
        is SpringFunSpec -> this.mockMvc
        else -> throw IllegalStateException("Your test class must extend SpringFunSpec")
    }

val StringSpec.mockMvc
    get() = when (this) {
        is SpringStringSpec -> this.mockMvc
        else -> throw IllegalStateException("Your test class must extend SpringStringSpec")
    }

val DescribeSpec.mockMvc
    get() = when (this) {
        is SpringDescribeSpec -> this.mockMvc
        else -> throw IllegalStateException("Your test class must extend SpringDescribeSpec")
    }

inline fun <reified T> FunSpec.mockBean(): T {
    val dis = when (this) {
        is SpringFunSpec -> this
        else -> throw IllegalStateException("Your test class must extend SpringFunSpec")
    }
    return dis.ctx.getBean(T::class.java)
}

inline fun <reified T> StringSpec.mockBean(): T {
    val dis = when (this) {
        is SpringStringSpec -> this
        else -> throw IllegalStateException("Your test class must extend SpringStringSpec")
    }
    return dis.ctx.getBean(T::class.java)
}

inline fun <reified T> DescribeSpec.mockBean(): T {
    val dis = when (this) {
        is SpringDescribeSpec -> this
        else -> throw IllegalStateException("Your test class must extend SpringDescribeSpec")
    }
    return dis.ctx.getBean(T::class.java)
}
