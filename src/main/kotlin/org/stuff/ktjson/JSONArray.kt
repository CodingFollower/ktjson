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