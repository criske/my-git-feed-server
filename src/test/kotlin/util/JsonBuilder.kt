package util

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

class JsonWriter internal constructor(
    private val mapper: ObjectMapper,
    private val json: JsonSerializable
) {

    fun asString(prettyPrint: Boolean = false): String {
        val writer = if (prettyPrint)
            this.mapper.writerWithDefaultPrettyPrinter()
        else
            this.mapper.writer()
        return writer.writeValueAsString(this.json)
    }
}

private class DslRawValue(value: Any) : RawValue(value, false)

private class ObjectScopeImpl(
    mapper: ObjectMapper,
    private val obj: ObjectNode
) : ObjectScope, BaseScopeDsl(mapper) {

    override fun Pair<String, Any>.unaryPlus() {
        put(this)
    }

    override fun put(value: Pair<String, Any>) {
        obj.putRawValue(value.first, DslRawValue(value.second))
    }
}

private class ArrayScopeImpl(
    mapper: ObjectMapper,
    private val arr: ArrayNode
) : ArrayScope, BaseScopeDsl(mapper) {

    override fun Any.unaryPlus() {
        add(this)
    }

    override fun add(any: Any) {
        arr.addRawValue(DslRawValue(any))
    }
}

interface ObjectScope : BaseScope {
    operator fun Pair<String, Any>.unaryPlus()
    fun put(value: Pair<String, Any>)
}

interface ArrayScope : BaseScope {
    operator fun Any.unaryPlus()
    fun add(any: Any)
}

interface BaseScope {
    fun obj(objScope: ObjectScope.() -> Unit): JsonSerializable
    fun arr(arrScope: ArrayScope.() -> Unit): JsonSerializable
}

abstract class BaseScopeDsl(private val mapper: ObjectMapper) : BaseScope {

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

val String.json get() = TextNode(this)
