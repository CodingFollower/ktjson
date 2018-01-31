package org.stuff.ktjson

import org.stuff.ktjson.error.InvalidJSONFormatException
import org.stuff.ktjson.error.KeyNotFoundException
import java.io.InputStream
import java.nio.charset.Charset

class JSONObject constructor() : JSONValueBase() {
    private val map = HashMap<String, JSONValue>()

    val allKeys: Set<String>
        get() = map.keys

    val isEmpty: Boolean
        get() = allKeys.isEmpty()

    init {
        type = JSONType.OBJECT
    }

    constructor(text: String) : this(JSONInputStreamReader(text), false)

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(stream, charset), false)

    internal constructor(reader: JSONInputStreamReader, ignoreLeft: Boolean): this() {
        parseJSONAndCheckLeft(reader, ignoreLeft) {
            parseObject(reader)
            reader.readNextChar()
        }
    }

    private fun parseObject(reader: JSONInputStreamReader) {
        if (reader.readFirstUnspaceChar() != '{') {
            throw InvalidJSONFormatException(reader.position, "Expect '{' at front of Object")
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
                throw InvalidJSONFormatException(reader.position, "Expert ',' between key value pairs in object")
            }

            reader.readNextUnspaceChar()
        } while (true)
    }

    private fun parseKeyValuePair(reader: JSONInputStreamReader) {
        if (reader.readFirstUnspaceChar() != '\"') {
            throw InvalidJSONFormatException(reader.position, "Expect '\"' at front of object key")
        }

        val key = reader.readString()
        if (key.isEmpty()) {
            throw InvalidJSONFormatException(reader.position, "Object key is empty")
        }

        if (reader.readFirstUnspaceChar() != ':') {
            throw InvalidJSONFormatException(reader.position, "Expect ':' between object key and value")
        }
        reader.readNextUnspaceChar()

        map[key] = reader.readJSONValue()
    }

    operator fun contains(key: String): Boolean {
        return key in allKeys
    }

    operator fun set(key: String, v: Boolean) {
        this[key] = JSONPrimitiveValue(v)
    }

    operator fun set(key: String, v: Number) {
        this[key] = JSONPrimitiveValue(v)
    }

    operator fun set(key: String, v: String) {
        this[key] = JSONPrimitiveValue(v)
    }

    operator fun set(key: String, v: JSONValue?) {
        map[key] = v ?: JSONPrimitiveValue()
    }

    operator fun get(key: String): JSONValue {
        return optGet(key) ?: throw KeyNotFoundException(key)
    }

    private fun optGet(key: String): JSONValue? {
        return if (key in this) map[key] else null
    }

    override fun optToJSONObject(): JSONObject? {
        return this
    }

    override fun formatToString(): String {
        val builder = StringBuilder()
        for ((key, value) in map) {
            builder.append("\"$key\":$value,")
        }

        if (builder.isNotEmpty()) {
            builder.deleteCharAt(builder.lastIndex)
        }
        return "{$builder}"
    }

    override fun toString(): String {
        return formatToString()
    }
}