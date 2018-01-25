package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.InvalidJSONFormatException
import org.stuff.ktjson.JSONObject
import org.stuff.ktjson.KeyNotFoundException
import kotlin.test.*

class JSONObjectTest {
    @Test
    fun emptyTest() {
        perform(listOf(JSONObject("{}"), JSONObject("   {   }   "), JSONObject())) {
            assertEquals(0, it.allKeys.size)
            assertTrue(it.isEmpty)
            assertFalse(it.contains("abc"))
            assertEquals("{}", it.toString())

            assertFailsWith<KeyNotFoundException> { it.isNullForKey("abc") }
            assertFailsWith<KeyNotFoundException> { it.getBoolean("abc") }
        }
    }

    @Test
    fun objectTest() {
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
        assertTrue(obj.isNullForKey("null_key"))
        assertEquals(true, obj.getBoolean("bool_key"))
        assertEquals(12, obj.getInteger("int_key"))
        assertEquals(12.01, obj.getDouble("double_key"))
        assertEquals("hello world", obj.getString("string_key"))

        val innerObj = obj.getObject("object_key")
        assertEquals(5, innerObj.allKeys.size)
        assertTrue(innerObj.isNullForKey("null_key"))
        assertEquals(false, innerObj.getBoolean("bool_key"))
        assertEquals(0, innerObj.getInteger("int_key"))
        assertEquals(0.1, innerObj.getDouble("double_key"))
        assertEquals("string", innerObj.getString("string_key"))

        val innerArray = obj.getArray("array_key")
        assertFalse(innerArray.isEmpty)
        assertEquals(5, innerArray.size)
    }

    @Test
    fun invalidObjectTest() {
        perform(listOf("{", "}", "{,}", "{key: }", "{\"key: }", "{\"key\": }", "{\"key\" 123 }", "\"key\": 123, }", "{\"key\": 123}  x")) {
            assertFailsWith<InvalidJSONFormatException> { JSONObject(it) }
        }
    }
}