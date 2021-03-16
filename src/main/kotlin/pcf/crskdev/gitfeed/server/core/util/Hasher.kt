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

package pcf.crskdev.gitfeed.server.core.util

import java.util.Base64

/**
 * Base64 encoding helper.
 *
 * @param items Items.
 * @param separator Separator.
 * @param isUrlEncoder Is url encoder.
 * @param padded Is padded.
 * @return Hash.
 */
fun base64Encode(
    vararg items: String,
    separator: String = "",
    isUrlEncoder: Boolean = false,
    padded: Boolean = true
): String {
    val bytes = items.joinToString(separator).toByteArray()
    val encoder = (
        if (isUrlEncoder)
            Base64.getUrlEncoder()
        else
            Base64.getEncoder()
        ).let { if (!padded) it.withoutPadding() else it }
    return encoder.encodeToString(bytes)
}

/**
 * Base64 decoding helper.
 *
 * @param hash Base64 encoded String.
 * @param isUrlDecoder Is url encoder.
 * @return Decoded String.
 */
fun base64Decode(
    hash: String,
    isUrlDecoder: Boolean = false,
): String {
    val decoder = if (isUrlDecoder)
        Base64.getUrlDecoder()
    else
        Base64.getDecoder()
    return String(decoder.decode(hash))
}
