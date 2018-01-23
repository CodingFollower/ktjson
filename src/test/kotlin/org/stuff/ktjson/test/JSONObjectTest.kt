package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONObject
import kotlin.test.assertNotNull

class JSONObjectTest {
    @Test
    fun emptyTest() {
        val obj = JSONObject("{}")
        assertNotNull(obj, "")
    }
}