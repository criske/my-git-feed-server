package pcf.crskdev.gitfeed.server.core.util

import com.fasterxml.jackson.databind.JsonSerializable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.util.RawValue

/**
 * Json object builder dsl for Jackson.
 *
 * @param scope [ObjectScope]
 * @receiver
 * @return JsonWriter
 */
fun obj(scope: ObjectScope.() -> Unit): JsonWriter {
    val mapper = ObjectMapper()
    val obj = mapper.createObjectNode()
    ObjectScopeImpl(mapper, obj).apply(scope)
    return JsonWriter(mapper, obj)
}

/**
 * Json array builder dsl for Jackson.
 *
 * @param scope [ArrayScope]
 * @receiver
 * @return JsonWriter
 */
fun arr(scope: ArrayScope.() -> Unit): JsonWriter {
    val mapper = ObjectMapper()
    val arr = mapper.createArrayNode()
    ArrayScopeImpl(mapper, arr).apply(scope)
    return JsonWriter(mapper, arr)
}

/**
 * Dumps the created json to string.
 *
 * @property mapper
 * @property json
 * @constructor Create empty Json writer
 */
class JsonWriter internal constructor(
    private val mapper: ObjectMapper,
    private val json: JsonSerializable
) {

    /**
     * As string with option of pretty printing.
     *
     * @param prettyPrint Pretty print flag.
     * @return JSON as string.
     */
    fun asString(prettyPrint: Boolean = false): String {
        val writer = if (prettyPrint)
            this.mapper.writerWithDefaultPrettyPrinter()
        else
            this.mapper.writer()
        return writer.writeValueAsString(this.json)
    }
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
        obj.putRawValue(this, DslRawValue(value.tryConvert()))
    }
}

private class ArrayScopeImpl(
    mapper: ObjectMapper,
    private val arr: ArrayNode
) : ArrayScope, BaseScopeDsl(mapper) {

    override fun Any.unaryPlus() {
        arr.addRawValue(DslRawValue(this.tryConvert()))
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
