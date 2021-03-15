package pcf.crskdev.gitfeed.server.impl.core.net

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import pcf.crskdev.gitfeed.server.core.net.Headers
import pcf.crskdev.gitfeed.server.core.net.RequestCommand
import pcf.crskdev.gitfeed.server.core.net.Response
import java.net.URI

/**
 * RequestCommand implementation based on [RestTemplate].
 *
 * @author Cristian Pela
 */
class RestTemplateCommand : RequestCommand {

    private val restTemplate = RestTemplate()

    override fun request(uri: URI, headers: Headers): Response {
        val restHeaders = RestHeaders().apply {
            headers.forEach { (key, values) ->
                addAll(key, values)
            }
        }
        val entity = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            HttpEntity<String>(restHeaders),
            String::class.java
        )
        return Response(
            entity.statusCode.value(),
            entity.body, entity.headers.toMap()
        )
    }
}

typealias RestHeaders = org.springframework.http.HttpHeaders
