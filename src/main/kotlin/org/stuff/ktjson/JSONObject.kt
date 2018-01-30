package org.stuff.ktjson

import java.io.InputStream
import java.nio.charset.Charset

class JSONObject constructor() : JSONValue() {
    private val map = HashMap<String, JSONValue>()

    val allKeys: Set<String>
        get() = map.keys

    val isEmpty: Boolean
        get() = allKeys.isEmpty()

    init {
        type = JSONType.OBJECT
    }

    constructor(text: String, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(text, charset), false)

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : this(JSONInputStreamReader(stream, charset), false)

    internal constructor(reader: JSONInputStreamReader, ignoreLeft: Boolean): this() {
        initJSON(reader, ignoreLeft) {
            parseObject(reader)
            reader.readNextChar()
        }
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

    fun contains(key: String): Boolean {
        return key in allKeys
    }

    fun isNullForKey(key: String): Boolean {
        return get(key).type == JSONType.NULL
    }

    fun putNull(key: String) {
        map[key] = JSONPrimitiveValue()
    }

    fun getBoolean(key: String): Boolean {
        return get(key).toBooleanValue()
    }

    fun getNumber(key: String): Double {
        return get(key).toNumberValue()
    }

    fun getString(key: String): String {
        return get(key).toStringValue()
    }

    fun getObject(key: String): JSONObject {
        val v = get(key)
        if (v.type != JSONType.OBJECT) {
            throw CastFailedException()
        }

        return v as JSONObject
    }

    fun getArray(key: String): JSONArray {
        val v = get(key)
        if (v.type != JSONType.ARRAY) {
            throw CastFailedException()
        }

        return v as JSONArray
    }

    private fun get(key: String): JSONValue {
        return optGet(key) ?: throw KeyNotFoundException(key)
    }

    private fun optGet(key: String): JSONValue? {
        return if (contains(key)) map[key] else null
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