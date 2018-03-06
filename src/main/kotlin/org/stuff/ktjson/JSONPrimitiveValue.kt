package org.stuff.ktjson

import org.stuff.ktjson.error.InvalidJSONFormatException
import java.util.regex.Pattern

internal fun parsePrimitiveValue(str: String): JSONPrimitiveValue {
    return JSONPrimitiveValue(JSONInputStreamReader(str), false)
}

internal class JSONPrimitiveValue internal constructor() : JSONValueBase() {
    private var booleanValue = false
    private var stringValue = ""

    private var numberValue: Double = 0.0

    internal constructor(bv: Boolean): this() {
        initBooleanValue(bv)
    }

    internal constructor(v: Number): this() {
        initDoubleValue(v.toDouble())
    }

    internal constructor(v: String): this() {
        initStringValue(v)
    }

    internal constructor(reader: JSONInputStreamReader, ignoreLeft: Boolean) : this() {
        parseJSONAndCheckLeft(reader, ignoreLeft) { parseValue(it) }
    }

    private  fun parseValue(reader: JSONInputStreamReader) {
        val ch = reader.readFirstUnspaceChar()
        when (ch) {
            '\"' -> initStringValue(reader.readString())
            'n' -> parseNull(reader)
            't' -> parseTrue(reader)
            'f' -> parseFalse(reader)
            else -> {
                val v = reader.readNextValidValue()
                initNumberValue(reader.position, v)
            }
        }
    }

    private fun parseNull(reader: JSONInputStreamReader) {
        if (!checkNextString(reader, "null")) {
            throw InvalidJSONFormatException(reader.position, "invalid null value")
        }

        type = JSONType.NULL
    }

    private fun parseTrue(reader: JSONInputStreamReader) {
        if (!checkNextString(reader, "true")) {
            throw InvalidJSONFormatException(reader.position, "invalid true value")
        }

        initBooleanValue(true)
    }

    private fun parseFalse(reader: JSONInputStreamReader) {
        if (!checkNextString(reader, "false")) {
            throw InvalidJSONFormatException(reader.position, "invalid false value")
        }

        initBooleanValue(false)
    }

    private fun checkNextString(reader: JSONInputStreamReader, expect: String): Boolean {
        val v = reader.readNextValidValue(expect.length)
        return v == expect
    }

    private fun initStringValue(v: String) {
        stringValue = v
        type = JSONType.STRING
    }

    private fun initBooleanValue(v: Boolean) {
        booleanValue = v
        type = JSONType.BOOL
    }

    private fun initNumberValue(pos: Long, str: String) {
        val  regex = """^-?(0|([1-9]\d*))(.\d+)?([e|E][+|-]?\d+)?$"""
        if (!Pattern.matches(regex, str)) {
            throw InvalidJSONFormatException(pos, "\"\$str\" not match regex \"$regex\"")
        }

        initDoubleValue(java.lang.Double.valueOf(str))
    }

    private fun initDoubleValue(v: Double) {
        numberValue = v
        type = JSONType.NUMBER
    }

    override fun optToBooleanValue() : Boolean? {
        return if (type == JSONType.BOOL) booleanValue else null
    }

    override fun optToNumberValue() : Double? {
        return if (type == JSONType.NUMBER) numberValue else null
    }

    override fun optToStringValue() : String? {
        return if (type == JSONType.STRING) stringValue else null
    }

    override fun toString(): String {
        return formatToString()
    }

    override fun formatToString(): String {
        when(type) {
            JSONType.NULL -> return "null"
            JSONType.BOOL -> return if (booleanValue) "true" else "false"
            JSONType.NUMBER -> return "$numberValue"
            JSONType.STRING -> return "\"${escapeString()}\""
            else -> return super.toString()
        }
    }

    private fun escapeString() : String {
        return escapeString(stringValue)
    }

    override fun cloneValue(): JSONValue {
        return when(type) {
            JSONType.NULL -> JSONPrimitiveValue()
            JSONType.BOOL -> JSONPrimitiveValue(booleanValue)
            JSONType.NUMBER -> JSONPrimitiveValue(numberValue)
            JSONType.STRING -> JSONPrimitiveValue(stringValue)
            else -> throw Exception("")
        }
    }
}