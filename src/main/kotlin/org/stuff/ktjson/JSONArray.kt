package org.stuff.ktjson

import java.io.InputStream
import java.nio.charset.Charset

class JSONArray : JSONValue {
    private val array = ArrayList<JSONValue>()

    init {
        type = JSONType.ARRAY
    }

    constructor(text: String, charset: Charset = Charsets.UTF_8) : super(JSONInputStreamReader(text, charset), true)

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : super(JSONInputStreamReader(stream, charset), true)

    internal constructor(reader: JSONInputStreamReader) : super(reader, false)

    override fun parseInternal(reader: JSONInputStreamReader) {
        parseArray(reader)
        reader.readNextChar()
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