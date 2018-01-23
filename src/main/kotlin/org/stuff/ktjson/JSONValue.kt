package org.stuff.ktjson

import java.nio.charset.Charset

open class JSONValue internal constructor(reader: JSONInputStreamReader, checkEnd: Boolean){
    var type: JSONType = JSONType.UNKNOW
        protected set
    private var booleanValue = false
    private var stringValue = ""
    private var numberValue: Double = 0.0

    init {
        parseInternal(reader)
        if (checkEnd && reader.isLeftContainsUnspace()) {
            throw InvalidJSONFormatException()
        }
    }

    internal constructor(text: String, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(text, charset), true)

    internal open fun parseInternal(reader: JSONInputStreamReader) {
        val ch = reader.readFirstUnspaceChar()
        if (ch == '\"') {
            stringValue = reader.readString()
            type = JSONType.STRING
        }
        else {
            val v = reader.readNextValidValue()
            when(v) {
                "null" -> type = JSONType.NULL
                "true" -> initBooleanValue(true)
                "false" -> initBooleanValue(false)
                else -> initNumberValue(v)
            }
        }
    }

    private fun initBooleanValue(v: Boolean) {
        booleanValue = v
        type = JSONType.BOOL
    }

    private fun initNumberValue(str: String) {
        try {
            numberValue = java.lang.Double.valueOf(str)
            type = JSONType.NUMBER
        }
        catch (e: NumberFormatException) {
            throw InvalidJSONFormatException(e.toString())
        }
    }

    override fun toString(): String {
        when(type) {
            JSONType.NULL -> return "null"
            JSONType.NUMBER -> return "$numberValue"
            JSONType.BOOL -> return if (booleanValue) "true" else "false"
            JSONType.STRING -> return "\"$stringValue\""
            else -> return super.toString()
        }
    }

    private fun<T> toTypeValue(block: () -> T?) : T {
        val v = block()
        if (v != null) {
            return v
        }

        throw CastFailedException()
    }

    internal fun toBooleanValue() : Boolean {
        return toTypeValue { optToBooleanValue() }
    }

    internal fun optToBooleanValue() : Boolean? {
        return if (type == JSONType.BOOL) booleanValue else null
    }

    internal fun toNumberValue() : Double {
        return toTypeValue { optToNumberValue() }
    }

    internal fun optToNumberValue() : Double? {
        return if (type == JSONType.NUMBER) numberValue else null
    }

    internal fun toStringValue() : String {
        return toTypeValue { optToStringValue() }
    }

    internal fun optToStringValue() : String? {
        return if (type == JSONType.STRING) stringValue else null
    }

    internal fun converToString() : String {
        val v = optToStringValue()
        if (v != null) {
            return v
        }

        return toString()
    }
}