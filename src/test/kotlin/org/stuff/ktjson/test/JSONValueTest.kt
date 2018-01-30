package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

fun invalidJSONValue(invalid : Collection<String>) {
    for (str in invalid) {
        val msg = "\"$str\" should be invalid json value"
        assertFailsWith<InvalidJSONFormatException>(msg){ parsePrimitiveValue(str) }
    }
}

fun<T> perform(list: Collection<T>, block: (T) -> Unit) {
    for (str in list) {
        block(str)
    }
}

fun<TKey, T> perform(map: Map<TKey, T>, block: (TKey, T) -> Unit) {
    for ((k, v) in map) {
        block(k, v)
    }
}

class JSONValueTest {
    @Test
    fun emptyTest() {
        invalidJSONValue(listOf(""))
    }

    @Test
    fun nullTest() {
        perform(listOf(JSONPrimitiveValue(),
                parsePrimitiveValue("null"),
                parsePrimitiveValue("  null  ")))
        {
            assertEquals(it.type, JSONType.NULL)
            assertEquals(it.toString(), "null")
            assertEquals(it.convertToString(), "null")

            assertFailsWith<CastFailedException> { it.toBooleanValue() }
            assertFailsWith<CastFailedException> { it.toNumberValue() }
            assertFailsWith<CastFailedException> { it.toStringValue() }
        }

        invalidJSONValue(listOf("null null", "Null", "NULL"))
    }

    @Test
    fun boolTest() {
        perform(mapOf(parsePrimitiveValue("true") to true,
                parsePrimitiveValue("false") to false,
                JSONPrimitiveValue(true) to true,
                JSONPrimitiveValue(false) to false))
        { v, expect ->
            assertEquals(JSONType.BOOL, v.type)
            assertEquals(expect, v.toBooleanValue())

            val str = if (expect) "true" else "false"
            assertEquals(str, v.toString())
            assertEquals(str, v.convertToString())

            assertFailsWith<CastFailedException> { v.toNumberValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

        invalidJSONValue(listOf("TRUE", "FALSE"))
    }

    @Test
    fun numberTest() {
        perform(mapOf(parsePrimitiveValue("0.1") to 0.1,
                parsePrimitiveValue("0.1") to 0.1,
                parsePrimitiveValue("0.1e0") to 0.1,
                parsePrimitiveValue("230.0012e2") to 23000.12,
                parsePrimitiveValue("230.0012E+2") to 23000.12,
                parsePrimitiveValue("230.0012E-2") to 2.300012,
                JSONPrimitiveValue(0.1) to 0.1,
                JSONPrimitiveValue(10.001) to 10.001))
        { v, expect ->
            assertEquals(JSONType.NUMBER, v.type)

            assertEquals(expect, v.toNumberValue())

            assertEquals("$expect", v.toString())
            assertEquals("$expect", v.convertToString())

            assertFailsWith<CastFailedException> { v.toBooleanValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

        perform(mapOf(parsePrimitiveValue("0") to 0,
                parsePrimitiveValue("-1") to -1,
                parsePrimitiveValue("-1024") to -1024,
                parsePrimitiveValue("1024") to 1024,
                parsePrimitiveValue("10.00") to 10,
                parsePrimitiveValue("-10.0") to -10,
                parsePrimitiveValue("10.01e2") to 1001,
                parsePrimitiveValue("0.1e2") to 10,
                parsePrimitiveValue("0.1e1") to 1,
                JSONPrimitiveValue(10) to 10, JSONPrimitiveValue(0) to 0))
        { v, expect ->
            assertEquals(JSONType.NUMBER, v.type)

            assertEquals(expect, v.toNumberValue().toInt())

            assertEquals("${expect.toDouble()}", v.toString())
            assertEquals("${expect.toDouble()}", v.convertToString())

            assertFailsWith<CastFailedException> { v.toBooleanValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

    }

    @Test
    fun invalidNumberTest() {
        invalidJSONValue(listOf("01", "+1", "--1", "0.e1", "0.1e++0", "0.1e", "0x0a"))
    }

    class StringExpect(val expect: String, val escaped: String = "\"$expect\"")

    @Test
    fun stringTest() {
        perform(mapOf("\"\"" to StringExpect(""),
                "\"Hello World\"" to StringExpect("Hello World"),
                """"Hello \r\nWorld"""" to StringExpect("Hello \r\nWorld", "\"Hello \\r\\nWorld\""),
                """"Hello \"World"""" to StringExpect("Hello \"World", "\"Hello \\\"World\""),
                """"Hello \u002AWorld"""" to StringExpect("Hello *World"),
                """"Hello \u002aWorld"""" to StringExpect("Hello *World")))
        { k, expect ->
            val v = parsePrimitiveValue(k)
            assertEquals(JSONType.STRING, v.type)
            assertEquals(expect.expect, v.toStringValue())
            assertEquals(expect.expect, v.convertToString())
            assertEquals(expect.escaped, v.toString())
            assertFailsWith<CastFailedException> { v.toNumberValue() }
            assertFailsWith<CastFailedException> { v.toBooleanValue() }

            val nv = JSONPrimitiveValue(expect.expect)
            assertEquals(JSONType.STRING, nv.type)
            assertEquals(JSONType.STRING, nv.type)
            assertEquals(expect.expect, nv.toStringValue())
            assertEquals(expect.expect, nv.convertToString())
            assertEquals(expect.escaped, nv.toString())
            assertFailsWith<CastFailedException> { nv.toNumberValue() }
            assertFailsWith<CastFailedException> { nv.toBooleanValue() }
        }

        invalidJSONValue(listOf("\"Hello \\U002AWorld\"", "\"Hello \\u2AWorld\"", "\"Hello World", "\"Hello \\World\""))
    }
}