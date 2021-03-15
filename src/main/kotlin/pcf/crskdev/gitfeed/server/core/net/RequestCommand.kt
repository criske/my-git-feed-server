package pcf.crskdev.gitfeed.server.core.net

import java.net.URI

/**
 * Request command for a GET.
 *
 * @author Pela Cristian
 */
interface RequestCommand {

    /**
     * Get Request
     *
     * @param uri URI
     * @param headers map of string to list string.
     * @return Response.
     */
    fun request(uri: URI, headers: Headers): Response
}
