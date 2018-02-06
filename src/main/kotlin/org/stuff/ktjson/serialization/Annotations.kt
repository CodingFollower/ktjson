package org.stuff.ktjson.serialization

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class JSONSerializeKeyName(val name: String)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class JSONSerializeIgnore