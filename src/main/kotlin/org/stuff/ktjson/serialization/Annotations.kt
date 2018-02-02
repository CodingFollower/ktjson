package org.stuff.ktjson.serialization

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class JSONSerializeKeyName(val name: String)