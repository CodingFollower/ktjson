package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONObject
import org.stuff.ktjson.serialization.JSONSerializeFailedException
import org.stuff.ktjson.serialization.deserialize
import org.stuff.ktjson.serialization.serialize
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class PrivateClass

class JSONSerializationTest {
    @Test
    fun invalidSerialize() {
        assertFailsWith<JSONSerializeFailedException> { serialize(PrivateClass()) }
    }

    @Test
    fun primitiveSerializeTest() {
        assertTrue(serialize(null).isNull())
        assertEquals(12, serialize(12).toNumberValue().toInt())
        assertEquals(0.001, serialize(0.001).toNumberValue())
        assertTrue(serialize(true).toBooleanValue())
        assertFalse(serialize(false).toBooleanValue())
        assertEquals("hello", serialize("hello").toStringValue())
    }

    @Test
    fun objectSerializeTest() {
        assertTrue(serialize(EmptyObject()).toJSONObject().isEmpty)

        val instance = TestObject()
        val obj = serialize(instance).toJSONObject()
        assertTrue("protectedProperty" !in obj)
        assertTrue("privateProperty" !in obj)
        assertTrue("renameProperty" !in obj)
        assertTrue("ignoreProperty1" !in obj)
        assertTrue("ignoreProperty2" !in obj)
        assertTrue("extProperty" !in obj)
        assertTrue("notFieldProperty" !in obj)

        assertTrue(obj["nullProperty"].isNull())
        assertEquals(instance.boolProperty, obj["boolProperty"].toBooleanValue())
        assertEquals(instance.strProperty, obj["strProperty"].toStringValue())
        assertEquals(instance.intProperty, obj["intProperty"].toNumberValue().toInt())
        assertEquals(instance.doubleProperty, obj["doubleProperty"].toNumberValue())
        assertEquals(instance.internalProperty, obj["internalProperty"].toStringValue())
        assertEquals(instance.renameProperty, obj["new_name"].toStringValue())
//        assertEquals(instance.notFieldProperty, obj["notFieldProperty"].toStringValue())

        val listArray = obj["listProperty"].toJSONArray()
        assertEquals(instance.listProperty.size, listArray.size)
        assertEquals((instance.listProperty[0] as TestObjectBase).strProperty, listArray[0].toJSONObject()["strProperty"].toStringValue())
        assertEquals(instance.listProperty[1], listArray[1].toStringValue())
        assertEquals(instance.listProperty[2], listArray[2].toNumberValue())

        val arrayJSON = obj["arrayProperty"].toJSONArray()
        assertEquals(instance.arrayProperty.size, arrayJSON.size)
        assertEquals((instance.arrayProperty[0]!! as TestObjectBase).intProperty, arrayJSON[0].toJSONObject()["intProperty"].toNumberValue().toInt())
        assertTrue(arrayJSON[1].isNull())
        assertEquals(instance.arrayProperty[2], arrayJSON[2].toStringValue())

        val innerObj = obj["objectProperty"].toJSONObject()
        assertEquals(instance.objectProperty.strProperty, innerObj["strProperty"].toStringValue())
        assertEquals(instance.objectProperty.intProperty, innerObj["intProperty"].toNumberValue().toInt())
        assertEquals(instance.objectProperty.boolProperty, innerObj["boolProperty"].toBooleanValue())
    }
}