package pcf.crskdev.gitfeed.server.core.net

typealias Headers = Map<String, List<String>>

fun Headers.first(key: String) = this[key]?.first()

fun headers(from: Headers = emptyMap(), body: HeadersScope.() -> Unit): Headers {
    val map = mutableMapOf<String, MutableList<String>>().apply {
        from.forEach { (key, value) ->
            put(key, value.toMutableList())
        }
    }

    class HeadersScopeImpl : HeadersScope {
        override fun String.to(value: String) {
            if (!map.containsKey(this)) {
                map[this] = mutableListOf()
            }
            map[this]?.add(value)
        }
    }
    HeadersScopeImpl().apply(body)
    return map
}

interface HeadersScope {
    infix fun String.to(value: String)
}
