package org.stuff.ktjson

import java.nio.charset.Charset
import java.util.regex.Pattern

internal fun initJSON(reader: JSONInputStreamReader, ignoreLeft: Boolean, block: (JSONInputStreamReader) -> Unit) {
    try {
        block(reader)
        if (!ignoreLeft && reader.isLeftContainsUnspace()) {
            throw InvalidJSONFormatException()
        }
    }
    catch (e: InvalidJSONFormatException) {
        throw e
    }
    catch (e: Exception) {
        throw InvalidJSONFormatException(e.toString())
    }
}

open class JSONValue constructor() {
    var type: JSONType = JSONType.UNKNOW
        protected set
    private var booleanValue = false
    private var stringValue = ""

    private var doubleValue: Double = 0.0
    private var intValue = 0

    internal constructor(reader: JSONInputStreamReader, ignoreLeft: Boolean) : this() {
        initJSON(reader, ignoreLeft) { parseValue(it) }
    }

    internal constructor(text: String, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(text, charset), false)

    private  fun parseValue(reader: JSONInputStreamReader) {
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
            val  regex = """^-?(0|([1-9]\d*))(.\d+)?([e|E][+|-]?\d+)?$"""
            if (!Pattern.matches(regex, str)) {
                throw InvalidJSONFormatException()
            }
            doubleValue = java.lang.Double.valueOf(str)
            type = JSONType.DOUBLE

            intValue = doubleValue.toInt()
            if (doubleValue.compareTo(intValue) == 0) {
                type = JSONType.INTEGER
            }
        }
        catch (e: Exception) {
            throw InvalidJSONFormatException(e.toString())
        }
    }

    override fun toString(): String {
        when(type) {
            JSONType.NULL -> return "null"
            JSONType.DOUBLE -> return "$doubleValue"
            JSONType.INTEGER -> return "$intValue"
            JSONType.BOOL -> return if (booleanValue) "true" else "false"
            JSONType.STRING -> return "\"${escapeString()}\""
            else -> return super.toString()
        }
    }

    private fun escapeString() : String {
        if (stringValue.isEmpty()) {
            return stringValue
        }

        val builder = StringBuilder(stringValue.length)
        for (ch in stringValue) {
            if (controlToCharMap.containsKey(ch)) {
                builder.append('\\')
                builder.append(controlToCharMap[ch])
            }
            else {
                builder.append(ch)
            }
        }

        return builder.toString()
    }

    private fun<T> toTypeValue(block: () -> T?) : T {
        return block() ?: throw CastFailedException()
    }

    internal fun toBooleanValue() : Boolean {
        return toTypeValue { optToBooleanValue() }
    }

    private fun optToBooleanValue() : Boolean? {
        return if (type == JSONType.BOOL) booleanValue else null
    }

    internal fun toDoubleValue() : Double {
        return toTypeValue { optToDoubleValue() }
    }

    private fun optToDoubleValue() : Double? {
        return if (type == JSONType.DOUBLE || type == JSONType.INTEGER) doubleValue else null
    }

    internal fun toIntegerValue() : Int {
        return toTypeValue { optToIntegerValue() }
    }

    private fun optToIntegerValue(): Int? {
        return if (type == JSONType.INTEGER) intValue else null
    }

    internal fun toStringValue() : String {
        return toTypeValue { optToStringValue() }
    }

    private fun optToStringValue() : String? {
        return if (type == JSONType.STRING) stringValue else null
    }

    internal fun convertToString() : String {
        val v = optToStringValue()
        if (v != null) {
            return v
        }

        return toString()
    }
}