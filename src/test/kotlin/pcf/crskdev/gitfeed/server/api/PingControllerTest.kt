package pcf.crskdev.gitfeed.server.api

import io.kotest.core.spec.style.StringSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pcf.crskdev.gitfeed.server.core.util.obj

@WebMvcTest(PingController::class)
class PingControllerTest @Autowired constructor(mockMvc: MockMvc) : StringSpec() {

    init {
        "should call ping api endpoint" {
            val json = obj {
                "ping" to "My-Git-Feed API is up and running"
            }
            mockMvc
                .perform(get("/api/ping"))
                .andDo(print()).andExpect(status().isOk)
                .andExpect(content().string(json.asString()))
        }
    }
}
