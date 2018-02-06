package org.stuff.ktjson.serialization

import org.stuff.ktjson.*
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter

internal fun walkClassSerializableProperty(cls: KClass<*>, instance: Any, block: (Any, KProperty<*>) -> Unit) {
    val properties = cls.declaredMemberProperties
    properties.map {
        if (it.findAnnotation<JSONSerializeIgnore>() != null) {
            return@map
        }

        if (it.javaField == null) {
            return@map
        }

        val getter = it.javaGetter?: return@map
        if (!checkMethodVisibility(getter)) {
            return@map
        }

        if (it !is KMutableProperty<*>) {
            return@map
        }
        val setter = it.javaSetter ?: return@map
        if (!checkMethodVisibility(setter)) {
            return@map
        }

        block(instance, it)
    }
}

internal fun getPropertyName(p: KProperty<*>): String {
    var name = p.name
    val a = p.findAnnotation<JSONSerializeKeyName>()
    if (a != null && !a.name.isEmpty()) {
        name = a.name
    }

    return name
}

internal fun checkMethodVisibility(call: Method): Boolean {
    val m = call.modifiers
    return Modifier.isPublic(m) && !Modifier.isStatic(m)
}

internal fun checkClassVisibility(cls: Class<*>): Boolean {
    val m = cls.modifiers
    return Modifier.isPublic(m) && !Modifier.isStatic(m)
}

fun serialize(v: Any?): JSONValue {
    return when {
        isPrimitiveValue(v) -> serializePrimitive(v) ?: throw JSONSerializeFailedException("$v cannot be serialize to JSON")
        v is Iterable<*> -> serializeIterable(v)
        v is kotlin.Array<*> -> serializeArray(v)
        else -> serializeObject(v!!)
    }
}

private fun serializeOnClass(cls: KClass<*>, instance: Any, obj: JSONObject) {
    walkClassSerializableProperty(cls, instance) {
        inst, p ->
        val name = getPropertyName(p)
        val getter = p.javaGetter?: throw JSONSerializeFailedException("getter of $name not exists")
        val pv = getter.invoke(inst)
        obj[name] = serialize(pv)
    }
}

private fun serializeClass(cls: KClass<*>, instance: Any, obj: JSONObject) {
    if (cls == Any::class || cls == JvmType.Object::class) {
        return
    }

    if (cls.findAnnotation<JSONSerializeIgnore>() != null) {
        return
    }

    cls.superclasses.map {
        serializeClass(it, instance, obj)
    }
    serializeOnClass(cls, instance, obj)
}

private fun serializeObject(instance: Any): JSONObject {
    val obj = JSONObject()
    val cls = instance::class

    if (!checkClassVisibility(cls.java)) {
        throw JSONSerializeFailedException("$cls not access")
    }

    serializeClass(cls, instance, obj)
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