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

package pcf.crskdev.gitfeed.server.core.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.util.RawValue
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * [ObjectMapper] with [KotlinModule] registered.
 *
 * @return ObjectMapper.
 */
@Suppress("FunctionName")
fun KObjectMapper(): ObjectMapper = ObjectMapper().apply {
    registerModule(KotlinModule())
}

private val K_OBJECT_MAPPER = KObjectMapper()

/**
 * Json object builder dsl for Jackson.
 *
 * @param scope [ObjectScope]
 * @receiver
 * @return JsonWriter
 */
fun obj(mapper: ObjectMapper = K_OBJECT_MAPPER, scope: ObjectScope.() -> Unit): JsonDump {
    val obj = mapper.createObjectNode()
    ObjectScopeImpl(mapper, obj).apply(scope)
    return JsonDump(mapper, obj)
}

/**
 * Json array builder dsl for Jackson.
 *
 * @param scope [ArrayScope]
 * @receiver
 * @return JsonWriter
 */
fun arr(mapper: ObjectMapper = K_OBJECT_MAPPER, scope: ArrayScope.() -> Unit): JsonDump {
    val arr = mapper.createArrayNode()
    ArrayScopeImpl(mapper, arr).apply(scope)
    return JsonDump(mapper, arr)
}

/**
 * Dumps the created json to string.
 *
 * @property mapper
 * @property json
 * @constructor Create empty Json writer
 */
class JsonDump internal constructor(
    private val mapper: ObjectMapper,
    private val json: JsonNode
) {

    /**
     * As string with option of pretty printing.
     *
     * @param prettyPrint Pretty print flag.
     * @return JSON as string.
     */
    fun asString(prettyPrint: Boolean = false): String {
        this.json
        val writer = if (prettyPrint)
            this.mapper.writerWithDefaultPrettyPrinter()
        else
            this.mapper.writer()
        return writer.writeValueAsString(this.json)
    }

    /**
     * As json tree.
     *
     * @return Root JsonNode.
     */
    fun asTree(): JsonNode = this.json
}

/**
 * Dsl raw value.
 *
 * @param value Any.
 */
private class DslRawValue(value: Any) : RawValue(value, false)

private class ObjectScopeImpl(
    mapper: ObjectMapper,
    private val obj: ObjectNode
) : ObjectScope, BaseScopeDsl(mapper) {

    override fun String.to(value: Any) {
        when (value) {
            is JsonNode -> obj.putPOJO(this, value)
            is NullNode -> obj.putNull(this)
            else -> obj.putRawValue(this, DslRawValue(value.tryConvert()))
        }
    }
}

private class ArrayScopeImpl(
    mapper: ObjectMapper,
    private val arr: ArrayNode
) : ArrayScope, BaseScopeDsl(mapper) {

    override fun Any.unaryPlus() {
        when (this) {
            is JsonNode -> arr.addPOJO(this)
            else -> arr.addRawValue(DslRawValue(this.tryConvert()))
        }
    }

    override fun add(number: Number) {
        arr.addRawValue(DslRawValue(number))
    }
}

/**
 * Try convert any value to a json serializable,
 * otherwise falls back to the same value.
 *
 * @return Any.
 */
private fun Any.tryConvert(): Any = when (this) {
    is String -> TextNode(this)
    else -> this
}

/**
 * Object scope
 *
 * @constructor Create empty Object scope
 */
interface ObjectScope : BaseScope {

    /**
     * Adds new entry in json object, having String receiver as key
     *
     * @param value Any value
     */
    infix fun String.to(value: Any)
}

/**
 * Array scope
 *
 * @constructor Create empty Array scope
 */
interface ArrayScope : BaseScope {
    /**
     * Unary plus that ads new entry to json array.
     *
     */
    operator fun Any.unaryPlus()

    /**
     * Adds a Number to the array, since [unaryPlus] cant be used.
     *
     * @param number
     */
    fun add(number: Number)
}

/**
 * Base scope.
 *
 * @constructor Create empty Base scope
 */
interface BaseScope {

    /**
     * Builder for json object.
     *
     * @param objScope ObjectScope
     * @receiver ObjectScope
     * @return JsonSerializable
     */
    fun obj(objScope: ObjectScope.() -> Unit): JsonSerializable

    /**
     * Builder for json array.
     *
     * @param arrScope ArrayScope
     * @receiver ObjectScope
     * @return JsonSerializable
     */
    fun arr(arrScope: ArrayScope.() -> Unit): JsonSerializable
}

private abstract class BaseScopeDsl(private val mapper: ObjectMapper) : BaseScope {

    override fun obj(objScope: ObjectScope.() -> Unit): JsonSerializable {
        return this.mapper.createObjectNode().apply {
            ObjectScopeImpl(mapper, this).apply(objScope)
        }
    }

    override fun arr(arrScope: ArrayScope.() -> Unit): JsonSerializable {
        return this.mapper.createArrayNode().apply {
            ArrayScopeImpl(mapper, this).apply(arrScope)
        }
    }
}
