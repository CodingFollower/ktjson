package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.CastFailedException
import org.stuff.ktjson.InvalidJSONFormatException
import org.stuff.ktjson.JSONArray
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
            assertFailsWith<ArrayIndexOutOfBoundsException> { it.isNullAt(0) }
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
        assertTrue(array.isNullAt(0))
        assertFailsWith<CastFailedException> { array.getNumber(1) }
        assertEquals(true, array.getBoolean(1))
        assertEquals(false, array.getBoolean(2))
        assertEquals(11, array.getNumber(3).toInt())
        assertEquals(0.001, array.getNumber(4))
        assertEquals("string", array.getString(5))

        assertFailsWith<CastFailedException> { array.getNumber(6) }
        val obj = array.getObject(6)
        assertEquals(2, obj.allKeys.size)

        val innerArray = array.getArray(7)
        assertEquals(4, innerArray.size)
        assertTrue(innerArray.isNullAt(0))
        assertEquals(true, innerArray.getBoolean(1))
        assertEquals(0, innerArray.getNumber(2).toInt())
        assertEquals("str", innerArray.getString(3))
    }

    @Test
    fun invalidTest() {
        perform(listOf("[", "]", "[,]", "[}")) {
            assertFailsWith<InvalidJSONFormatException> { JSONArray(it) }
        }
    }
}