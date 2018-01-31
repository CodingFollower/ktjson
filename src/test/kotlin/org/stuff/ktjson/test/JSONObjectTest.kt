package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.*
import kotlin.test.*

class JSONObjectTest {
    @Test
    fun emptyTest() {
        perform(listOf(JSONObject("{}"), JSONObject("   {   }   "), JSONObject())) {
            assertEquals(0, it.allKeys.size)
            assertTrue(it.isEmpty)
            assertFalse("abc" in it)
            assertEquals("{}", it.toString())

            assertFailsWith<KeyNotFoundException> { it["abc"] }
        }
    }

    @Test
    fun objectParseTest() {
        val str = """
            |{
                |"null_key": null,
                |"bool_key": true,
                |"int_key": 12,
                |"double_key": 12.01,
                |"string_key": "hello world",
                |"object_key": {
                |   "null_key": null,
                |   "bool_key": false,
                |   "int_key": 0,
                |   "double_key": 0.1,
                |   "string_key": "string"
                |},
                |"array_key": [
                |   null, true, 10, 11.1, "str"
                |]
            |}
            |""".trimMargin()


        val obj = JSONObject(str)
        assertFalse(obj.isEmpty)
        assertEquals(7, obj.allKeys.size)
        assertTrue(obj["null_key"].isNull())
        assertEquals(true, obj["bool_key"].toBooleanValue())
        assertEquals(12, obj["int_key"].toNumberValue().toInt())
        assertEquals(12.01, obj["double_key"].toNumberValue())
        assertEquals("hello world", obj["string_key"].toStringValue())

        val innerObj = obj["object_key"].toJSONObject()
        assertEquals(5, innerObj.allKeys.size)
        assertTrue(innerObj["null_key"].isNull())
        assertEquals(false, innerObj["bool_key"].toBooleanValue())
        assertEquals(0, innerObj["int_key"].toNumberValue().toInt())
        assertEquals(0.1, innerObj["double_key"].toNumberValue())
        assertEquals("string", innerObj["string_key"].toStringValue())

        val innerArray = obj["array_key"].toJSONArray()
        assertFalse(innerArray.isEmpty)
        assertEquals(5, innerArray.size)
    }

    @Test
    fun invalidObjectTest() {
        perform(listOf("{", "}", "{,}", "{key: }", "{\"key: }", "{\"key\": }", "{\"key\" 123 }", "\"key\": 123, }", "{\"key\": 123}  x")) {
            assertFailsWith<InvalidJSONFormatException> { JSONObject(it) }
        }
    }

    @Test
    fun editObjectTest() {
        val obj = JSONObject()
        obj["key"] = null
        assertTrue(obj["key"].isNull())

        obj["key"] = true
        assertFalse(obj["key"].isNull())
        assertTrue(obj["key"].toBooleanValue())

        obj["key"] = 12
        assertFailsWith<CastFailedException> { obj["key"].toBooleanValue() }
        assertEquals(12, obj["key"].toNumberValue().toInt())

        obj["key"] = "hello \tworld"
        assertEquals("hello \tworld", obj["key"].toStringValue())
        assertEquals("\"hello \\tworld\"", obj["key"].formatToString())

        val innerObj = JSONObject()
        obj["key"] = innerObj
        obj["key"].toJSONObject()["key"] = 12.01
        assertEquals(12.01, obj["key"].toJSONObject()["key"].toNumberValue())

        val innerArray = JSONArray()
        obj["key"] = innerArray
        obj["key"].toJSONArray().add(0.001)
        assertEquals(0.001, obj["key"].toJSONArray()[0].toNumberValue())
    }
}