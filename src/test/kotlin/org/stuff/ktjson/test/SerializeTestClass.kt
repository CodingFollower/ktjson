package org.stuff.ktjson.test

import org.stuff.ktjson.serialization.JSONSerializeIgnore
import org.stuff.ktjson.serialization.JSONSerializeKeyName

class EmptyObject

class InvalidObject(var name: String)

open class IgnoreClass1 {
    var ignoreProperty1 = "ignore1"
}

@JSONSerializeIgnore
open class IgnoreClass2 : IgnoreClass1() {
    var ignoreProperty2 = "ignore2"
}

open class TestObjectBase : IgnoreClass2() {
    var boolProperty = false
    var strProperty: String = "str"
    var intProperty: Int = 10

    protected var protectedProperty = "protected"
}

class TestObject : TestObjectBase() {
    var doubleProperty: Double = 0.01
    var nullProperty: String? = null

    @JSONSerializeKeyName("new_name")
    var renameProperty = "rename"

    var listProperty = listOf(TestObjectBase(), "text", 100.01)
    var arrayProperty = arrayOf(TestObjectBase(), null, "text")

    var objectProperty = TestObjectBase()

    @JSONSerializeIgnore
    var ignoreProperty = "ignore"

    private var privateProperty = "private"
    internal var internalProperty = "internal"

    var notFieldProperty
        get() = protectedProperty
        set(value) {
            protectedProperty = value
        }
}

val TestObject.extProperty: String
    get() = strProperty