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

import mu.KLogger
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty

/**
 * K logger simple builder function.
 *
 * @return mu.KLogger.
 */
inline fun <reified T> KLogger(): KLogger = KotlinLogging
    .logger(LoggerFactory.getLogger(T::class.java))

/**
 * K logger simple builder delegation function.
 *
 * @return mu.KLogger.
 */
fun logger(): ReadOnlyProperty<Any, KLogger> =
    ReadOnlyProperty { thisRef, _ ->
        KotlinLogging
            .logger(LoggerFactory.getLogger(thisRef::class.java))
    }
