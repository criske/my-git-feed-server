package pcf.crskdev.gitfeed.server.core.net

/**
 * Access token used by [RequestCommand] via [RequestClient]
 * to make authenticated requests.
 *
 * @property key
 * @property value
 * @constructor Create empty Access token
 */
data class AccessToken(val key: String, val value: String) {

    companion object {
        /**
         * U n a u t h o r i z e d token.
         */
        val UNAUTHORIZED = AccessToken("Authorization", "")
    }
}
