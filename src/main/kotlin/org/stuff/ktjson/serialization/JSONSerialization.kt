package org.stuff.ktjson.serialization

import org.stuff.ktjson.*
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

fun serialize(v: Any?): JSONValue {
    return when {
        isPrimitiveValue(v) -> serializePrimitive(v) ?: throw JSONSerializeFailedException("$v cannot be serialize to JSON")
        v is Iterable<*> -> serializeIterable(v)
        v is kotlin.Array<*> -> serializeArray(v)
        else -> serializeObject(v!!)
    }
}

private fun serializeObject(instance: Any): JSONObject {
    val obj = JSONObject()
    val cls = instance::class
    val properties = cls.memberProperties
    for (p in properties) {
        if (p.javaField == null) {
            continue
        }

        val getter = p.javaGetter ?: continue
        val m = getter.modifiers
        if (!Modifier.isPublic(m) || Modifier.isStatic(m)) {
            continue
        }
        val pv = getter.invoke(instance)

        var name = p.name
        val a = p.findAnnotation<JSONSerializeKeyName>()
        if (a != null && !a.name.isEmpty()) {
            name = a.name
        }
        obj[name] = serialize(pv)
    }

    return obj
}

private fun isPrimitiveValue(v: Any?): Boolean {
    return v == null
            || v is Boolean
            || v is Number
            || v is String
}

private fun serializePrimitive(v: Any?): JSONValue? {
    return when {
        v == null -> JSONPrimitiveValue()
        v is Boolean -> JSONPrimitiveValue(v)
        v is Number -> JSONPrimitiveValue(v)
        v is String -> JSONPrimitiveValue(v)
        else -> null
    }
}

private fun serializeIterable(array: Iterable<*>): JSONArray {
    val json = JSONArray()
    for (v in array) {
        json.add(serialize(v))
    }

    return json
}

private fun serializeArray(array: kotlin.Array<*>): JSONArray {
    val json = JSONArray()
    for (v in array) {
        json.add(serialize(v))
    }

    return json
}

fun<T : Any> deserialize(cls: KClass<T>, v: JSONValue): T {
    val constructor = cls.constructors.find { it.parameters.isEmpty() }
            ?: throw JSONDeserializeFailedException("$cls must has a constructor without parameters")
    val instance = constructor.call()

    return instance
}

private fun deserializePrimitive(v: JSONPrimitiveValue): Any? {
    return when(v.type) {
        JSONType.NULL -> null
        JSONType.BOOL -> v.toBooleanValue()
        JSONType.NUMBER -> v.toNumberValue()
        JSONType.STRING -> v.toStringValue()
        else -> throw JSONDeserializeFailedException("")
    }
}