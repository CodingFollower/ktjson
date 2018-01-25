package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONArray
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JSONArrayTest {
    @Test
    fun emptyTest() {
        perform(listOf(JSONArray("[]"), JSONArray("   [   ]   "), JSONArray())) {
            assertEquals(0, it.size)
            assertTrue(it.isEmpty)
        }
    }
}