package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.serialization.JSONSerializeFailedException
import org.stuff.ktjson.serialization.serialize
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JSONSerializationTest {
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
    fun lateinitSerializeTest() {
        val obj = LateinitPropertyTestClass()
        assertFailsWith<InvocationTargetException> { serialize(obj) }

        obj.init()
        val json = serialize(obj).toJSONObject()
        assertEquals(obj.lateinitProperty.property, json["lateinitProperty"].toJSONObject()["property"].toStringValue())
    }

    @Test
    fun nonDefaultConstructorSerializeTest() {
        assertFailsWith<JSONSerializeFailedException> { serialize(NonDefaultConstructorTestClass("")) }
    }

    @Test
    fun nonserializablePropertyTest() {
        val obj = NonserializablePropertyTestClass()
        val json = serialize(obj).toJSONObject()
        assertTrue(json.isEmpty)
    }

    @Test
    fun compositionSerializeTest() {
        val obj = CompositionTestClass()
        val json = serialize(obj).toJSONObject()
        assertEquals(obj.stringProperty, json["stringProperty"].toStringValue())

        val innerObj = json["objectProperty"].toJSONObject()
        assertEquals(obj.objectProperty.property, innerObj["property"].toStringValue())

        val list = json["listProperty"].toJSONArray()
        assertEquals(obj.listProperty.size, list.size)
        for (i in 0 until obj.listProperty.size) {
            assertEquals(obj.listProperty[i].property, list[i].toJSONObject()["property"].toStringValue())
        }
    }

    @Test
    fun collectionSerializeTest() {
        val list = listOf(ElementTestClass("data1"),
                ElementTestClass("data2"),
                ElementTestClass("data3"))
        var listJson = serialize(list).toJSONArray()
        assertEquals(list.size, listJson.size)
        for (i in 0 until list.size) {
            assertEquals(list[i].property, listJson[i].toJSONObject()["property"].toStringValue())
        }

        val set = setOf("data1", "data2", "data3")
        val setJson = serialize(set).toJSONArray()
        assertEquals(set.size, setJson.size)
        for (i in 0 until setJson.size) {
            assertTrue(setJson[i].toStringValue() in set)
        }

        val array = arrayOf("array1", "array2", "array3")
        val arrayJson = serialize(array).toJSONArray()
        assertEquals(array.size, arrayJson.size)
        for (i in 0 until array.size) {
            assertEquals(array[i], arrayJson[i].toStringValue())
        }
    }

    @Test
    fun mapSerializeTest() {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val mapJson = serialize(map).toJSONObject()
        assertEquals(map.size, mapJson.allKeys.size)
        for ((k, v) in map) {
            assertEquals(v, mapJson[k].toStringValue())
        }
    }

    @Test
    fun inheritClassSerializeTest() {
        var obj = BasicTestClass()
        var baseJson = serialize(obj).toJSONObject()
        assertTrue(baseJson["nullableProperty"].isNull())
        assertEquals(obj.boolProperty, baseJson["boolProperty"].toBooleanValue())
        assertEquals(obj.intProperty, baseJson["intProperty"].toNumberValue().toInt())
        assertEquals(obj.doubleProperty, baseJson["doubleProperty"].toNumberValue())
        assertEquals(obj.stringProperty, baseJson["stringProperty"].toStringValue())
        assertEquals("base", baseJson["baseProperty"].toStringValue())
        assertTrue("baseReadonlyProperty" !in baseJson)

        var inherit: BasicTestClass = InheritTestClass()
        var inheritJson = serialize(inherit).toJSONObject()
        assertTrue(inheritJson["nullableProperty"].isNull())
        assertEquals(inherit.boolProperty, inheritJson["boolProperty"].toBooleanValue())
        assertEquals(inherit.intProperty, inheritJson["intProperty"].toNumberValue().toInt())
        assertEquals(inherit.doubleProperty, inheritJson["doubleProperty"].toNumberValue())
        assertEquals(inherit.stringProperty, inheritJson["stringProperty"].toStringValue())
        assertEquals((inherit as InheritTestClass).inheritProperty, inheritJson["inheritProperty"].toStringValue())
        assertEquals("override", inheritJson["baseProperty"].toStringValue())
        assertEquals("override_readwrite", inheritJson["baseReadonlyProperty"].toStringValue())
    }

    @Test
    fun renamePropertySerializeTest() {
        val obj = RenamePropertyTestClass()
        val json = serialize(obj).toJSONObject()
        assertTrue("renameProperty" !in json)
        assertEquals(obj.renameProperty, json["rename_property"].toStringValue())
    }

    @Test
    fun ignorePropertySerializeTest() {
        val obj = IgnorePropertyTestClass()
        val json = serialize(obj).toJSONObject()
        assertEquals(obj.property, json["property"].toStringValue())
        assertTrue("ignoreProperty" !in json)
    }

    @Test
    fun ignoreClassSerializeTest() {
        val obj = InheritFromIgnoreTestClass()
        val json = serialize(obj).toJSONObject()
        assertTrue("baseProperty" !in json)
        assertEquals(obj.inheritProperty, json["inheritProperty"].toStringValue())
    }
}