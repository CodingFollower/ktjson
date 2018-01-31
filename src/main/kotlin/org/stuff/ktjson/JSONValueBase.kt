package org.stuff.ktjson

abstract class JSONValueBase : JSONValue {
    override var type: JSONType = JSONType.NULL
        protected set

    override fun isNull(): Boolean {
        return type == JSONType.NULL
    }

    private fun<T> toTypeValue(block: () -> T?) : T {
        return block() ?: throw CastFailedException()
    }

    override fun toBooleanValue(): Boolean {
        return toTypeValue { optToBooleanValue() }
    }

    override fun optToBooleanValue(): Boolean? {
        return null
    }

    override fun toNumberValue(): Double {
        return toTypeValue { optToNumberValue() }
    }

    override fun optToNumberValue(): Double? {
        return null
    }

    override fun toStringValue(): String {
        return toTypeValue { optToStringValue() }
    }

    override fun optToStringValue(): String? {
        return null
    }

    override fun toJSONObject(): JSONObject {
        return toTypeValue { optToJSONObject() }
    }

    override fun optToJSONObject(): JSONObject? {
        return null
    }

    override fun toJSONArray(): JSONArray {
        return toTypeValue { optToJSONArray() }
    }

    override fun optToJSONArray(): JSONArray? {
        return null
    }

    override fun convertToString(): String {
        val v = optToStringValue()
        if (v != null) {
            return v
        }

        return toString()
    }
}