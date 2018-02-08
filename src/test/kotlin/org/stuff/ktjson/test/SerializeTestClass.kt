package org.stuff.ktjson.test

import org.stuff.ktjson.serialization.JSONSerializeIgnore
import org.stuff.ktjson.serialization.JSONSerializeKeyName

class CompositionTestClass {
    var stringProperty = "string"
    var objectProperty = ElementTestClass("object")
    var listProperty = listOf(ElementTestClass("data1"),
            ElementTestClass("data2"),
            ElementTestClass("data3"))
}

class ElementTestClass() {
    var property = ""

    constructor(p: String): this() {
        property = p
    }
}

open class NonserializablePropertyTestClass {
    val readonlyProperty = "readonly"
    var privateSetProperty = "private_set"
        private set

    private var privateProperty = "private"
    protected var protectedProperty = "protected"
    var nonFieldProperty
        get() = privateProperty
        set(value) {privateProperty = value}
}

var NonserializablePropertyTestClass.extProperty
    get() = nonFieldProperty
    set(value) {nonFieldProperty = value}

open class BasicTestClass {
    var nullableProperty: String? = null
    var boolProperty = false
    var intProperty = 10
    var doubleProperty = 10.0
    var stringProperty = "string"

    open var baseProperty = "base"
}

class InheritTestClass : BasicTestClass() {
    var inhertProperty = "inhert"

    override var baseProperty = "override"
}

class RenamePropertyTestClass {
    @JSONSerializeKeyName("rename_property")
    var renameProperty = "rename"
}

class IgnorePropertyTestClass {
    var property = "property"

    @JSONSerializeIgnore
    var ignoreProperty = "ignore"
}

@JSONSerializeIgnore
open class IgnoreBaseTestClass {
    var baseProperty = "base"
}

class InheritFromIgnoreTestClass : IgnoreBaseTestClass() {
    var inheritProperty = "inherit"
}

class NonDefaultConstructorTestClass(var name: String)

class LateinitPropertyTestClass {
    lateinit var lateinitProperty: ElementTestClass

    fun init() {
        lateinitProperty = ElementTestClass("lateinit")
    }
}

open class TestObjectBase {
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