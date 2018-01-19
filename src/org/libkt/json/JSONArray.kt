package org.openkt.json

import java.io.ByteArrayInputStream
import java.io.InputStream

class JSONArray : JSONValue {
    private val array = ArrayList<JSONValue>()

    init {
        type = JSONType.ARRAY
    }

    constructor(text: String) : this(ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)))

    constructor(stream: InputStream) : this(JSONInputStreamReader(stream))

    internal constructor(reader: JSONInputStreamReader) {
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