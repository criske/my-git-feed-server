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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JsonBuilderTest : StringSpec({

    "should create simple json object" {
        val json = obj {
            "name" to "cris"
            "age" to 18
        }

        json.asString() shouldBe """{"name":"cris","age":18}"""
    }

    "should create nested json object" {
        val json = obj {
            "name" to "cris"
            "age" to 18
            "address" to obj {
                "street" to "franklin"
                "no" to 3
                "location" to obj {
                    "latitude" to 45.4
                    "longitude" to 120.4
                }
            }
        }
        json.asString() shouldBe """{"name":"cris","age":18,"address":{"street":"franklin","no":3,"location":{"latitude":45.4,"longitude":120.4}}}"""
    }

    "should create nested json array" {
        val json = arr {
            +obj { "name" to "cris" }
            +obj {
                "address" to obj {
                    "street" to "franklin"
                    "no" to 3
                }
                "hobbies" to arr {
                    +"Music"
                    +"Football"
                    add(7)
                }
            }
        }
        json.asString() shouldBe """[{"name":"cris"},{"address":{"street":"franklin","no":3},"hobbies":["Music","Football",7]}]"""
    }

    "should add kotlin object to json" {
        class User(val firstName: String, val lastName: String, val age: Int)

        val json = obj {
            "admin" to User("john", "doe", 32)
            "users" to arr {
                +User("frank", "done", 22)
                +User("mary", "jane", 22)
            }
        }

        json.asString() shouldBe """{"admin":{"firstName":"john","lastName":"doe","age":32},"users":[{"firstName":"frank","lastName":"done","age":22},{"firstName":"mary","lastName":"jane","age":22}]}"""
    }

    "should accept nulls" {
        obj {
            "foo" to null
        }.asString() shouldBe """{"foo":null}"""
    }
})
