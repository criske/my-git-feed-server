package pcf.crskdev.gitfeed.server.core.net

import com.fasterxml.jackson.databind.JsonNode

/**
 * A successful Response converted to JSON tree. (2xx, 3xx)
 *
 * @property body
 * @property headers
 * @author Cristian Pela
 */
class JsonResponse(val body: JsonNode, val headers: Headers)
