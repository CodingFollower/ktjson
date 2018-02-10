package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONArray
import org.stuff.ktjson.JSONObject
import org.stuff.ktjson.serialization.JSONDeserializeFailedException
import org.stuff.ktjson.serialization.deserialize
import org.stuff.ktjson.serialization.jsonobjectOf
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
    fun lateinitPropertyDeserializeTest() {
        val json = jsonobjectOf("lateinitProperty" to "string")
        val obj = deserialize(LateinitPropertyTestClass::class, json)
        assertEquals(json["lateinitProperty"].toStringValue(), obj.lateinitProperty)
    }

    @Test
    fun nonDefaultConstructorTest() {
        assertFailsWith<JSONDeserializeFailedException> { deserialize(NonDefaultConstructorTestClass::class, JSONObject()) }
    }

    @Test
    fun invalidValueTypeTest() {
        class InvalidPropertyTypeTestClass {
            var doubleProperty = 0.1
        }

        val obj = jsonobjectOf("doubleProperty" to 0.1)
        deserialize(InvalidPropertyTypeTestClass::class, obj)

        obj["doubleProperty"] = "text"
        assertFailsWith<JSONDeserializeFailedException> { deserialize(InvalidPropertyTypeTestClass::class, obj) }
    }

    @Test
    fun missPropertyTest() {
        class MissPropertyTestClass {
            var property = "property"
        }

        val obj = jsonobjectOf("property" to "string")
        assertEquals(obj["property"].toStringValue(), deserialize(MissPropertyTestClass::class, obj).property)

        obj.delete("property")
        assertFailsWith<JSONDeserializeFailedException> { deserialize(MissPropertyTestClass::class, obj) }
    }

    @Test
    fun deserializeTest() {
        val obj = deserialize(TestObject::class, createJSONObject())
        assertEquals(0.01, obj.doubleProperty)
    }
}