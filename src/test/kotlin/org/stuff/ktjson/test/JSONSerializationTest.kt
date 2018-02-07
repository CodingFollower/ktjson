package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONArray
import org.stuff.ktjson.serialization.serialize
import kotlin.test.assertEquals
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
    fun basicClassSerializeTest() {
        var obj = BasicTestClass()
        var json = serialize(obj).toJSONObject()
        assertTrue(json["nullableProperty"].isNull())
        assertEquals(obj.boolProperty, json["boolProperty"].toBooleanValue())
        assertEquals(obj.intProperty, json["intProperty"].toNumberValue().toInt())
        assertEquals(obj.doubleProperty, json["doubleProperty"].toNumberValue())
        assertEquals(obj.stringProperty, json["stringProperty"].toStringValue())
    }

    @Test
    fun inheritClassSerializeTest() {
        var obj: BasicTestClass = InheritTestClass()
        var json = serialize(obj).toJSONObject()
        assertTrue(json["nullableProperty"].isNull())
        assertEquals(obj.boolProperty, json["boolProperty"].toBooleanValue())
        assertEquals(obj.intProperty, json["intProperty"].toNumberValue().toInt())
        assertEquals(obj.doubleProperty, json["doubleProperty"].toNumberValue())
        assertEquals(obj.stringProperty, json["stringProperty"].toStringValue())
        assertEquals((obj as InheritTestClass).inhertProperty, json["inhertProperty"].toStringValue())
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

    @Test
    fun objectSerializeTest() {
        assertTrue(serialize(EmptyTestClass()).toJSONObject().isEmpty)

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