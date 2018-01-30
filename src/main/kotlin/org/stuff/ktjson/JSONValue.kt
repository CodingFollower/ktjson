package org.stuff.ktjson

abstract class JSONValue {
    var type: JSONType = JSONType.NULL
        protected set

    private fun<T> toTypeValue(block: () -> T?) : T {
        return block() ?: throw CastFailedException()
    }

    fun toBooleanValue(): Boolean {
        return toTypeValue { optToBooleanValue() }
    }

    open fun optToBooleanValue(): Boolean? {
        return null
    }

    fun toNumberValue(): Double {
        return toTypeValue { optToNumberValue() }
    }

    open fun optToNumberValue(): Double? {
        return null
    }

    fun toStringValue(): String {
        return toTypeValue { optToStringValue() }
    }

    open fun optToStringValue(): String? {
        return null
    }

    fun toJSONObject(): JSONObject {
        return toTypeValue { optToJSONObject() }
    }

    open fun optToJSONObject(): JSONObject? {
        return null
    }

    fun toJSONArray(): JSONArray {
        return toTypeValue { optToJSONArray() }
    }

    open fun optToJSONArray(): JSONArray? {
        return null
    }

    fun convertToString(): String {
        val v = optToStringValue()
        if (v != null) {
            return v
        }

        return toString()
    }

    abstract fun formatToString(): String
}