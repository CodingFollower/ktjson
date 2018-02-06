package org.stuff.ktjson.serialization

import org.stuff.ktjson.JSONObject
import org.stuff.ktjson.JSONType
import org.stuff.ktjson.JSONValue
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.cast
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.javaSetter

fun<T : Any> deserialize(cls: KClass<T>, obj: JSONObject): T {
    val constructor = cls.constructors.find { it.parameters.isEmpty() }
            ?: throw JSONDeserializeFailedException("$cls must has a constructor without parameters")
    val instance = constructor.call()
    walkClassSerializableProperty(cls, instance) {
        inst, p ->
        val name = getPropertyName(p)
        if (name !in obj) {
            throw JSONDeserializeFailedException("property $name not found in json")
        }

        val setter = (p as KMutableProperty<*>).javaSetter
                ?: throw JSONDeserializeFailedException("setter of $name not exists")
        val parameters = setter.parameters
        if (parameters.size != 1) {
            throw JSONDeserializeFailedException("${p.name} set parameters count must be 1")
        }

        val v = obj[name]
        val type = parameters[0].type.kotlin
        if (v.isNull()) {
            if (!p.returnType.isMarkedNullable) {
                throw JSONDeserializeFailedException("$name not nullable")
            }
            setter.invoke(inst,null)
        }
        else if (type.isSubclassOf(Boolean::class)) {
            checkJSONValueType(JSONType.BOOL, v)
            setter.invoke(inst, v.toBooleanValue())
        }
        else if (type.isSubclassOf(Number::class)) {
            checkJSONValueType(JSONType.NUMBER, v)
            val num = v.toNumberValue()
            when {
                type.isSubclassOf(Byte::class) -> setter.invoke(inst, num.toByte())
                type.isSubclassOf(Short::class) -> setter.invoke(inst, num.toShort())
                type.isSubclassOf(Int::class) -> setter.invoke(inst, num.toInt())
                type.isSubclassOf(Long::class) -> setter.invoke(inst, num.toLong())
                type.isSubclassOf(Float::class) -> setter.invoke(inst, num.toFloat())
                else -> setter.invoke(inst, num)
            }
        }
        else if (type.isSubclassOf(String::class)) {
            checkJSONValueType(JSONType.STRING, v)
            setter.invoke(inst, v.toStringValue())
        }
        else if (v.type == JSONType.ARRAY) {
        }
        else if (v.type == JSONType.OBJECT) {
            setter.invoke(inst, deserialize(type, v.toJSONObject()))
        }
    }

    return instance
}

private fun checkJSONValueType(expect: JSONType, v: JSONValue) {
    if (v.type != expect) {
        throw JSONDeserializeFailedException("expect $expect actual ${v.type}")
    }
}