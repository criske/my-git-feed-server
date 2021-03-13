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
})
