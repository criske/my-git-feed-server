package pcf.crskdev.gitfeed.server.core

import com.fasterxml.jackson.core.JsonParseException
import pcf.crskdev.gitfeed.server.core.util.KObjectMapper
import pcf.crskdev.gitfeed.server.core.util.obj
import java.io.IOException

/**
 * Git feed exception.
 *
 * @property type Error type.
 * @property customMessage Message.
 * @author Cristian Pela.
 */
class GitFeedException(
    private val type: Type,
    private val customMessage: String?
) : RuntimeException() {

    companion object {
        private val MAPPER = KObjectMapper()
    }

    enum class Type {
        IO, HTTP, VALIDATION
    }

    override val message: String
        get() = obj(MAPPER) {
            "type" to type.name.toLowerCase()
            "error" to (
                customMessage?.let {
                    try {
                        MAPPER.readTree(customMessage)
                    } catch (ex: JsonParseException) {
                        it // if can't parse, fallback to simple string
                    }
                } ?: "unknown"
                )
        }.asString()
}

fun IOException.toGitFeedException() = GitFeedException(
    GitFeedException.Type.IO,
    this.message
)
