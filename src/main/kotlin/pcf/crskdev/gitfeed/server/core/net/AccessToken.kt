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

package pcf.crskdev.gitfeed.server.core.net

import pcf.crskdev.gitfeed.server.core.GitFeedException
import pcf.crskdev.gitfeed.server.core.util.base64Encode

/**
 * Access token used by [RequestCommand] via [RequestClient]
 * to make authenticated requests.
 *
 * @property key
 * @property value
 * @constructor Create empty Access token
 */
interface AccessToken {
    val key: String
    val value: String
}

/**
 * Unauthorized token.
 *
 */
object Unauthorized : AccessToken {

    private val exception = GitFeedException.fromString(
        type = GitFeedException.Type.VALIDATION,
        "The access token for current provider is not set"
    )

    override val key: String
        get() = throw exception
    override val value: String
        get() = throw exception
}

/**
 * Bearer token authorization.
 *
 * @property token Token.
 */
class Bearer(private val token: String) : AccessToken {
    override val key: String
        get() = "Authorization"
    override val value: String
        get() = "Bearer $token"
}

/**
 * Basic token authorization
 *
 * @property username Username
 * @property password Password.
 */
class Basic(
    private val username: String,
    private val password: String
) : AccessToken {

    companion object {
        /**
         * Basic Access token with encoding already provided.
         *
         * @param encoded Base64 encoded username and password.
         * @return AccessToken.
         */
        fun withEncoded(encoded: String): AccessToken = object : AccessToken {
            override val key: String
                get() = "Authorization"
            override val value: String
                get() = "Basic $encoded"
        }
    }

    /**
     * Encoded credentials.
     */
    private val encoded = base64Encode(username, password, separator = ":")

    override val key: String
        get() = "Authorization"
    override val value: String
        get() = "Basic $encoded"
}
