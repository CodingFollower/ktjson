package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.CastFailedException
import org.stuff.ktjson.InvalidJSONFormatException
import org.stuff.ktjson.JSONType
import org.stuff.ktjson.JSONValue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class JSONValueTest {
    private fun invalidJSONValue(invalid : Collection<String>) {
        for (str in invalid) {
            val msg = "\"$str\" should be invalid json value"
            assertFailsWith<InvalidJSONFormatException>(msg){ JSONValue(str) }
        }
    }

    private fun perform(list: Collection<String>, block: (String) -> kotlin.Unit) {
        for (str in list) {
            block(str)
        }
    }

    @Test
    fun emptyTest() {
        invalidJSONValue(listOf(""))
    }

    @Test
    fun nullTest() {
        perform(listOf("null", "  null  ")) {
            val v = JSONValue(it)
            assertEquals(v.type, JSONType.NULL)
            assertEquals(v.toString(), "null")
            assertEquals(v.converToString(), "null")

            assertFailsWith<CastFailedException> { v.toBooleanValue() }
            assertFailsWith<CastFailedException> { v.toNumberValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

        invalidJSONValue(listOf("null null", "Null", "NULL"))
    }

    @Test
    fun boolTest() {
        perform(listOf("true", "false", "  true  ", "  false  ")) {
            val v = JSONValue(it)
            assertEquals(v.type, JSONType.BOOL)
            assertEquals(v.toBooleanValue(), it.trim() == "true")
            assertEquals(v.toString(), it.trim())
            assertEquals(v.converToString(), it.trim())

            assertFailsWith<CastFailedException> { v.toNumberValue() }
            assertFailsWith<CastFailedException> { v.toStringValue() }
        }

        invalidJSONValue(listOf("TRUE", "FALSE"))
    }
}