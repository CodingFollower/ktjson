package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.*
import org.stuff.ktjson.error.InvalidJSONFormatException
import org.stuff.ktjson.error.KeyNotFoundException
import org.stuff.ktjson.error.TypeErrorException
import java.io.ByteArrayInputStream
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
    fun deleteKeyTest() {
        val str = """
            |{
            |   "key": "value",
            |   "key1": "value"
            |}
            |""".trimMargin()
        val obj = JSONObject(str)
        assertEquals(2, obj.allKeys.size)
        assertEquals(null, obj.delete("not_exists"))

        val v = obj.delete("key")
        assertNotEquals(null, v)
        assertEquals("value", v!!.toStringValue())
        assertEquals(1, obj.allKeys.size)
        assertEquals("value", obj["key1"].toStringValue())
    }

    @Test
    fun objectParseTest() {
        val str = """
            |{
                |"null_key": null,
                |"bool_key": true,
                |"int_key": 12,
                |"double_key": 12.01,
                |"string_key": "你好",
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

        perform(listOf(JSONObject(str),
                JSONObject(ByteArrayInputStream(str.toByteArray(Charsets.UTF_32)), Charsets.UTF_32)))
        {
            assertFalse(it.isEmpty)
            assertEquals(7, it.allKeys.size)
            assertTrue(it["null_key"].isNull())
            assertEquals(true, it["bool_key"].toBooleanValue())
            assertEquals(12, it["int_key"].toNumberValue().toInt())
            assertEquals(12.01, it["double_key"].toNumberValue())
            assertEquals("你好", it["string_key"].toStringValue())

            val innerObj = it["object_key"].toJSONObject()
            assertEquals(5, innerObj.allKeys.size)
            assertTrue(innerObj["null_key"].isNull())
            assertEquals(false, innerObj["bool_key"].toBooleanValue())
            assertEquals(0, innerObj["int_key"].toNumberValue().toInt())
            assertEquals(0.1, innerObj["double_key"].toNumberValue())
            assertEquals("string", innerObj["string_key"].toStringValue())

            val innerArray = it["array_key"].toJSONArray()
            assertFalse(innerArray.isEmpty)
            assertEquals(5, innerArray.size)
        }
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
        assertFailsWith<TypeErrorException> { obj["key"].toBooleanValue() }
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