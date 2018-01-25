package org.stuff.ktjson

import java.io.InputStream
import java.nio.charset.Charset

class JSONArray constructor() : JSONValue() {
    private val array = ArrayList<JSONValue>()

    val size: Int
        get() = array.size

    val isEmpty: Boolean
        get() = array.isEmpty()

    init {
        type = JSONType.ARRAY
    }

    constructor(text: String, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(text, charset), false)

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(stream, charset), false)

    internal constructor(reader: JSONInputStreamReader, ignoreLeft: Boolean) : this() {
        initJSON(reader, ignoreLeft) {
            parseArray(reader)
            reader.readNextChar()
        }
    }

    private fun parseArray(reader: JSONInputStreamReader) {
        if (reader.readFirstUnspaceChar() != '[') {
            throw InvalidJSONFormatException("Expect '[' at front of Array")
        }

        if (reader.readNextUnspaceChar() == ']') {
            return
        }

        do {
            val v = reader.readJSONValue()
            array.add(v)

            val ch = reader.readFirstUnspaceChar()
            if (ch == ']') {
                break
            }

            if (ch != ',') {
                throw InvalidJSONFormatException("Expert ',' between values")
            }

            reader.readNextUnspaceChar()
        } while (true)
    }

    fun isNullAt(idx: Int): Boolean {
        return get(idx).type == JSONType.NULL
    }

    fun getBoolean(idx: Int): Boolean {
        return get(idx).toBooleanValue()
    }

    fun getInteger(idx: Int): Int {
        return get(idx).toIntegerValue()
    }

    fun getDouble(idx: Int): Double {
        return get(idx).toDoubleValue()
    }

    fun getString(idx: Int): String {
        return get(idx).toStringValue()
    }

    fun getObject(idx: Int): JSONObject {
        val v = get(idx)
        if (v.type != JSONType.OBJECT) {
            throw CastFailedException()
        }

        return v as JSONObject
    }

    fun getArray(idx: Int): JSONArray {
        val v = get(idx)
        if (v.type != JSONType.ARRAY) {
            throw CastFailedException()
        }

        return v as JSONArray
    }

    private fun get(idx: Int): JSONValue {
        return optGet(idx) ?: throw ArrayIndexOutOfBoundsException("$idx out of range(0, ${if (size > 0) size - 1 else 0})")
    }

    private fun optGet(idx: Int): JSONValue? {
        return if(idx < 0 || idx >= size) null else array[idx]
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (value in array) {
            builder.append("$value,")
        }

        if (builder.isNotEmpty()) {
            builder.deleteCharAt(builder.lastIndex)
        }
        return "[$builder]"
    }
}