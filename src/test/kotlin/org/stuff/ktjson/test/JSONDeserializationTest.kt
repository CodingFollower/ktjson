package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONArray
import org.stuff.ktjson.JSONObject
import org.stuff.ktjson.serialization.JSONDeserializeFailedException
import org.stuff.ktjson.serialization.deserialize
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JSONDeserializationTest {
    private fun createJSONObject(): JSONObject {
        val obj = JSONObject()
        obj["boolProperty"] = true
        obj["strProperty"] = "string"
        obj["intProperty"] = 12

        val root = obj.cloneValue() as JSONObject
        root["doubleProperty"] = 0.01
        root["nullProperty"] = null
        root["new_name"] = "rename value"

        val listJSON = JSONArray()
        listJSON.add("element1")
        listJSON.add("element2")
        root["listProperty"] = listJSON

        val arrayJSON = JSONArray()
        arrayJSON.add(10)
        arrayJSON.add(-1)
        root["arrayProperty"] = arrayJSON

        root["objectProperty"] = obj.cloneValue()
        root["internalProperty"] = "internal"

        return root
    }

    @Test
    fun invalidTest() {
        assertFailsWith<JSONDeserializeFailedException> { deserialize(NonDefaultConstructorTestClass::class, JSONObject()) }

        var obj = createJSONObject()
        obj["doubleProperty"] = true
        assertFailsWith<JSONDeserializeFailedException> { deserialize(TestObject::class, obj) }

        obj = createJSONObject()
        obj["doubleProperty"] = null
        assertFailsWith<JSONDeserializeFailedException> { deserialize(TestObject::class, obj) }
    }

    @Test
    fun deserializeTest() {
        val obj = deserialize(TestObject::class, createJSONObject())
        assertEquals(0.01, obj.doubleProperty)
    }
}