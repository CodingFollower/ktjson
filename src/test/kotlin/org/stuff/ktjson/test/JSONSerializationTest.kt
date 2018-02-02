package org.stuff.ktjson.test

import org.junit.Test
import org.stuff.ktjson.JSONPrimitiveValue
import org.stuff.ktjson.serialization.deserialize
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
    fun primitiveDeserializeTest() {
        assertEquals(null, deserialize(JSONPrimitiveValue()))
        assertEquals(true, deserialize(JSONPrimitiveValue(true)))
        assertEquals(false, deserialize(JSONPrimitiveValue(false)))
        assertEquals(12.toDouble(), deserialize(JSONPrimitiveValue(12)))
        assertEquals(0.01, deserialize(JSONPrimitiveValue(0.01)))
        assertEquals("word", deserialize(JSONPrimitiveValue("word")))
    }

    private class PrivateObject

    open class TestObjectBase {
        val boolProperty = false
        val strProperty: String = "str"
        val intProperty: Int = 10

        protected val protectedProperty = "protected"
    }

    class TestObject : TestObjectBase() {
        val doubleProperty: Double = 0.01
        val nullProperty = null

        val listProperty = listOf<Any>(TestObjectBase(), "text", 100.01)
        val arrayProperty = arrayOf(TestObjectBase(), null)

        val objetProperty = TestObjectBase()

        private val privateProperty = "private"
        internal val internalProperty = "internal"

        val notFieldProperty
            get() = protectedProperty
    }

    val TestObject.extProperty: String
        get() = strProperty

    @Test
    fun objectSerializeTest() {
        assertTrue(serialize(PrivateObject()).toJSONObject().isEmpty)

        val obj = serialize(TestObject()).toJSONObject()
        assertTrue("protectedProperty" !in obj)
        assertTrue("privateProperty" !in obj)
        assertTrue("notFieldProperty" !in obj)
        assertTrue("extProperty" !in obj)

        assertTrue(obj["nullProperty"].isNull())
        assertFalse(obj["boolProperty"].toBooleanValue())
        assertEquals("str", obj["strProperty"].toStringValue())
        assertEquals(10, obj["intProperty"].toNumberValue().toInt())
        assertEquals(0.01, obj["doubleProperty"].toNumberValue())
        assertEquals("internal", obj["internalProperty"].toStringValue())

        val listArray = obj["listProperty"].toJSONArray()
        assertEquals(3, listArray.size)
        assertEquals("str", listArray[0].toJSONObject()["strProperty"].toStringValue())
        assertEquals("text", listArray[1].toStringValue())
        assertEquals(100.01, listArray[2].toNumberValue())

        val arrayJSON = obj["arrayProperty"].toJSONArray()
        assertEquals(2, arrayJSON.size)
        assertEquals(10, arrayJSON[0].toJSONObject()["intProperty"].toNumberValue().toInt())
        assertTrue(arrayJSON[1].isNull())

        val innerObj = obj["objetProperty"].toJSONObject()
        assertEquals("str", innerObj["strProperty"].toStringValue())
        assertEquals(10, innerObj["intProperty"].toNumberValue().toInt())
        assertFalse(innerObj["boolProperty"].toBooleanValue())
    }
}