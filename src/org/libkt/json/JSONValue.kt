package org.libkt.json

open class JSONValue constructor() {
    var type: JSONType = JSONType.UNKNOW
    private var booleanValue = false
    private var stringValue = ""
    private var numberValue: Double = 0.0

    internal constructor(reader: JSONInputStreamReader): this() {
        val ch = reader.readFirstUnspaceChar()
        when {
            ch == '\"' -> {
                stringValue = reader.readString()
                type = JSONType.STRING
            }
            else -> {
                val v = reader.readNextValidValue()
                when(v) {
                    "null" -> type = JSONType.NULL
                    "true" -> initBooleanValue(true)
                    "false" -> initBooleanValue(false)
                    else -> initNumberValue(v)
                }
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
}