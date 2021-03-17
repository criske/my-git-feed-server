/*
 * MIT License
 *
 *  Copyright (c) 2021. Pela Cristian
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 */

package pcf.crskdev.gitfeed.server.core

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import pcf.crskdev.gitfeed.server.core.util.JsonDump
import pcf.crskdev.gitfeed.server.core.util.KObjectMapper
import pcf.crskdev.gitfeed.server.core.util.obj
import java.io.IOException

/**
 * Git feed exception.
 *
 * @property type Error type.
 * @property json JSON message.
 * @author Cristian Pela.
 */
class GitFeedException(
    private val type: Type,
    private val json: JsonNode? = null
) : RuntimeException() {

    companion object {
        private val MAPPER = KObjectMapper()
        val UNKNOWN = fromString(Type.UNKNOWN, "Unknown error")

        /**
         * From string.
         *
         * @param type Type.
         * @param message Message.
         * @param isJsonStr Flags that this message is in JSON format
         */
        fun fromString(
            type: Type,
            message: String? = "Unknown error",
            isJsonStr: Boolean = false
        ): GitFeedException {
            val json = if (isJsonStr && message != null) {
                try {
                    MAPPER.readTree(message)
                } catch (ex: JsonParseException) {
                    TextNode(message)
                }
            } else {
                TextNode(message ?: "Unknown error")
            }
            return GitFeedException(type, json)
        }
    }

    enum class Type {
        IO, HTTP, VALIDATION, UNKNOWN
    }

    override val message: String
        get() = this.errorDump().asString()

    /**
     * As json.
     *
     * @return JsonNode.
     */
    fun asJson(): JsonNode = this.errorDump().asTree()

    /**
     * Error json dump.
     *
     * @return JsonDump.
     */
    private fun errorDump(): JsonDump = obj(MAPPER) {
        "type" to type.name.toLowerCase()
        "error" to (json ?: "Unknown error")
    }
}

fun IOException.toGitFeedException() = GitFeedException.fromString(
    GitFeedException.Type.IO,
    this.message ?: "Unknown IO error"
)
