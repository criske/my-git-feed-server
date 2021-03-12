package pcf.crskdev.gitfeed.server.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pcf.crskdev.gitfeed.server.core.Ping

@RestController
@RequestMapping("/api")
class PingController {

    @GetMapping("/ping")
    fun ping(): Ping = Ping()
}
