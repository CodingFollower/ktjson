package org.stuff.ktjson

import org.stuff.ktjson.error.InvalidJSONFormatException
import org.stuff.ktjson.error.TypeErrorException

internal fun parseJSONAndCheckLeft(reader: JSONInputStreamReader, ignoreLeft: Boolean, block: (JSONInputStreamReader) -> Unit) {
    block(reader)
    if (!ignoreLeft && reader.isLeftContainsUnspace()) {
        throw InvalidJSONFormatException(reader.position, "Stream contains unnecessary chars after json text")
    }
}

abstract class JSONValueBase : JSONValue {
    override var type: JSONType = JSONType.NULL
        protected set

    override fun isNull(): Boolean {
        return type == JSONType.NULL
    }

    private fun<T> toTypeValue(expect: JSONType, block: () -> T?) : T {
        return block() ?: throw TypeErrorException(expect, type)
    }

    override fun toBooleanValue(): Boolean {
        return toTypeValue(JSONType.BOOL) { optToBooleanValue() }
    }

    override fun optToBooleanValue(): Boolean? {
        return null
    }

    override fun toNumberValue(): Double {
        return toTypeValue(JSONType.NUMBER) { optToNumberValue() }
    }

    override fun optToNumberValue(): Double? {
        return null
    }

    override fun toStringValue(): String {
        return toTypeValue(JSONType.STRING) { optToStringValue() }
    }

    override fun optToStringValue(): String? {
        return null
    }

    override fun toJSONObject(): JSONObject {
        return toTypeValue(JSONType.OBJECT) { optToJSONObject() }
    }

    override fun optToJSONObject(): JSONObject? {
        return null
    }

    override fun toJSONArray(): JSONArray {
        return toTypeValue(JSONType.ARRAY) { optToJSONArray() }
    }

    override fun optToJSONArray(): JSONArray? {
        return null
    }

    override fun convertToString(): String {
        return optToStringValue() ?: formatToString()
    }
}