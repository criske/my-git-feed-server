package pcf.crskdev.gitfeed.server.api

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pcf.crskdev.gitfeed.server.core.util.obj
import pcf.crskdev.gitfeed.server.util.SpringFunSpec
import pcf.crskdev.gitfeed.server.util.mockMvc

@WebMvcTest(PingController::class)
class PingControllerTest : SpringFunSpec({

    val json = obj {
        "ping" to "My-Git-Feed API is up and running"
    }

    test("should call ping api endpoint") {
        mockMvc
            .perform(get("/api/ping"))
            .andDo(print()).andExpect(status().isOk)
            .andExpect(content().string(json.asString()))
    }
})
