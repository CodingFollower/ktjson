package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.error.TypeErrorException
import org.stuff.ktjson.error.InvalidJSONFormatException
import org.stuff.ktjson.JSONArray
import org.stuff.ktjson.JSONObject
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JSONArrayTest {
    @Test
    fun emptyTest() {
        perform(listOf(JSONArray("[]"), JSONArray("   [   ]   "), JSONArray())) {
            assertEquals(0, it.size)
            assertTrue(it.isEmpty)
            assertFailsWith<ArrayIndexOutOfBoundsException> { it[0] }
        }
    }

    @Test
    fun arrayParseTest() {
        val str = """
            |[
            |   null,
            |   true,
            |   false,
            |   11,
            |   0.1e-2,
            |   "string",
            |   {
            |       "key1": "value1",
            |       "key2": "value2"
            |   },
            |   [
            |       null,
            |       true,
            |       0,
            |       "str"
            |   ]
            |]
        """.trimMargin()

        val array = JSONArray(str)
        assertEquals(8, array.size)
        assertFalse(array.isEmpty)
        assertTrue(array[0].isNull())
        assertFailsWith<TypeErrorException> { array[1].toNumberValue() }
        assertEquals(true, array[1].toBooleanValue())
        assertEquals(false, array[2].toBooleanValue())
        assertEquals(11, array[3].toNumberValue().toInt())
        assertEquals(0.001, array[4].toNumberValue())
        assertEquals("string", array[5].toStringValue())

        assertFailsWith<TypeErrorException> { array[6].toNumberValue() }
        val obj = array[6].toJSONObject()
        assertEquals(2, obj.allKeys.size)

        val innerArray = array[7].toJSONArray()
        assertEquals(4, innerArray.size)
        assertTrue(innerArray[0].isNull())
        assertEquals(true, innerArray[1].toBooleanValue())
        assertEquals(0, innerArray[2].toNumberValue().toInt())
        assertEquals("str", innerArray[3].toStringValue())
    }

    @Test
    fun invalidTest() {
        perform(listOf("[", "]", "[,]", "[}")) {
            assertFailsWith<InvalidJSONFormatException> { JSONArray(it) }
        }
    }

    @Test
    fun editArrayTest() {
        val array = JSONArray()
        array.add(null)
        array.add(true)
        array.add(12)
        array.add(0.1)
        array.add("hello \tworld")

        array.add(JSONObject())
        array[5].toJSONObject()["key"] = false

        array.add(JSONArray())
        array[6].toJSONArray().add(null)

        assertEquals(7, array.size)
        assertTrue(array[0].isNull())
        assertEquals(true, array[1].toBooleanValue())
        assertEquals(12, array[2].toNumberValue().toInt())
        assertEquals(0.1, array[3].toNumberValue())
        assertEquals("hello \tworld", array[4].toStringValue())
        assertEquals("\"hello \\tworld\"", array[4].formatToString())
        assertEquals(false, array[5].toJSONObject()["key"].toBooleanValue())
        assertTrue(array[6].toJSONArray()[0].isNull())

        assertFailsWith<ArrayIndexOutOfBoundsException> { array[7] = "word" }

        array[1] = "word"
        assertEquals("word", array[1].toStringValue())
    }
}