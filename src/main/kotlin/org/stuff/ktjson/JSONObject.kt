package org.stuff.ktjson

import java.io.InputStream
import java.nio.charset.Charset

class JSONObject : JSONValue {
    private val map = HashMap<String, JSONValue>()

    init {
        type = JSONType.OBJECT
    }

    constructor(text: String, charset: Charset = Charsets.UTF_8) : super(JSONInputStreamReader(text, charset), true)

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : super(JSONInputStreamReader(stream, charset), true)

    internal constructor(reader: JSONInputStreamReader) : super(reader, false)

    override fun parseInternal(reader: JSONInputStreamReader) {
        parseObject(reader)
        reader.readNextChar()
    }

    private fun parseObject(reader: JSONInputStreamReader) {
        if (reader.readFirstUnspaceChar() != '{') {
            throw InvalidJSONFormatException("Expect '{' at front of Object")
        }

        if (reader.readNextUnspaceChar() == '}') {
            return
        }

        do {
            parseKeyValuePair(reader)
            val ch = reader.readFirstUnspaceChar()
            if (ch == '}') {
                break
            }

            if (ch != ',') {
                throw InvalidJSONFormatException("Expert ',' between key value pairs")
            }

            reader.readNextUnspaceChar()
        } while (true)
    }

    private fun parseKeyValuePair(reader: JSONInputStreamReader) {
        if (reader.readFirstUnspaceChar() != '\"') {
            throw InvalidJSONFormatException("Expect '\"' at front of object key")
        }

        val key = reader.readString()
        if (key.isEmpty()) {
            throw InvalidJSONFormatException("Object key is empty")
        }

        if (reader.readFirstUnspaceChar() != ':') {
            throw InvalidJSONFormatException("Expect ':' between object key and value")
        }
        reader.readNextUnspaceChar()

        map[key] = reader.readJSONValue()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for ((key, value) in map) {
            builder.append("\"$key\": $value,")
        }

        if (builder.isNotEmpty()) {
            builder.deleteCharAt(builder.lastIndex)
        }
        return "{$builder}"
    }
}