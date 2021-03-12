package pcf.crskdev.gitfeed.server.api

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.FunSpec
import io.kotest.spring.SpringListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import util.json
import util.obj

@WebMvcTest(PingController::class)
class PingControllerTest : FunSpec({
    with(this as PingControllerTest) {

        val json = obj {
            +("ping" to "My-Git-Feed API is up and running".json)
        }
        test("should call ping api endpoint") {
            mockMvc
                .perform(get("/api/ping"))
                .andDo(print()).andExpect(status().isOk)
                .andExpect(content().string(json.asString()))
        }
    }
}) {

    @Autowired
    lateinit var mockMvc: MockMvc

    override fun listeners(): List<TestListener> {
        return listOf(SpringListener)
    }
}
