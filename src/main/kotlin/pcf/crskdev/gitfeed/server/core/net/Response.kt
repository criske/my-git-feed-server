package pcf.crskdev.gitfeed.server.core.net

/**
 * Wrapper around a http Response.
 *
 * @property code Status code.
 * @property body Body as string
 * @property headers Http headers
 * @author Cristian Pela
 */
class Response(val code: Int, val body: String?, val headers: Headers)
