package pcf.crskdev.gitfeed.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyGitFeedServerApplication

fun main(args: Array<String>) {
	runApplication<MyGitFeedServerApplication>(*args)
}
