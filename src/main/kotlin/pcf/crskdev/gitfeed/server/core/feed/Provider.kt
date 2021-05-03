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

package pcf.crskdev.gitfeed.server.core.feed

import pcf.crskdev.gitfeed.server.core.net.AccessToken
import pcf.crskdev.gitfeed.server.core.net.Basic
import pcf.crskdev.gitfeed.server.core.net.Bearer
import pcf.crskdev.gitfeed.server.core.net.Unauthorized

/**
 * Git platform Provider.
 *
 * @param accessToken Access token.
 * @author Cristian Pela
 */
enum class Provider(val accessToken: AccessToken) {

    BITBUCKET(Basic.withEncoded(System.getenv("BB_TOKEN"))),

    GITHUB(Bearer(System.getenv("GH_TOKEN"))),

    GITLAB(Bearer(System.getenv("GL_TOKEN"))),

    UNKNOWN(Unauthorized);

    companion object {
        /**
         * Safe version of enum valueOf() that returns default if value by name is
         * not found.
         *
         * @param name Value name
         * @param default Default value.
         * @return Found value or default.
         */
        fun valueOfSafe(name: String, default: Provider = UNKNOWN): Provider =
            try {
                Provider.valueOf(name.trim().toUpperCase())
            } catch (ex: IllegalArgumentException) {
                default
            }
    }
}
