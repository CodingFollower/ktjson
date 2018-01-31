package org.stuff.ktjson

interface JSONValue {
    val type: JSONType

    fun isNull(): Boolean

    fun toBooleanValue(): Boolean
    fun optToBooleanValue(): Boolean?

    fun toNumberValue(): Double
    fun optToNumberValue(): Double?

    fun toStringValue(): String
    fun optToStringValue(): String?

    fun toJSONObject(): JSONObject
    fun optToJSONObject(): JSONObject?

    fun toJSONArray(): JSONArray
    fun optToJSONArray(): JSONArray?

    fun convertToString(): String

    fun formatToString(): String
}