package org.openkt.json

import com.sun.javaws.exceptions.InvalidArgumentException
import java.io.*
import java.nio.charset.Charset

private val jsonControlMap = mapOf('\"' to '\"', '\\' to '\\', '/' to '/',
        'b' to '\b', 'f' to (12).toChar(),
        'n' to '\n', 'r' to '\r', 't' to '\t')
private val jsonValueEncloseChars = arrayOf('}', ']', ',')

private fun isSpace(ch: Char) : Boolean {
    return ch == ' '
            || ch == '\t'
            || ch == '\r'
            || ch == '\n'
}

class JSONInputStreamReader(stream : InputStream, private val charset: Charset = Charsets.UTF_8) {
    private val reader = InputStreamReader(stream, charset)
    private var lastReadValidChar: Char = (0).toChar()

    val currentChar: Char
        get() {
            if (lastReadValidChar == (0).toChar()) {
                readNextValidChar()
            }

            return lastReadValidChar
        }

    internal fun readJSONValue(): JSONValue {
        val ch = readFirstUnspaceChar()
        when(ch) {
            '{' -> return JSONObject(this)
            '[' -> return JSONArray(this)
            else -> return JSONValue(this)
        }
    }

    fun readNextValidValue(): String {
        val builder = StringBuilder()
        do {
            val ch = readFirstUnspaceChar()
            if (isSpace(ch)
                    || (ch in jsonValueEncloseChars)) {
                break
            }

            builder.append(ch)

            if (readNextChar() == -1) {
                break
            }

        } while (true)

        return builder.toString()
    }

    fun readString(): String {
        if (readFirstUnspaceChar() != '\"') {
            throw InvalidJSONFormatException("Expect '\"' at front of string")
        }

        val builder = StringBuilder()
        do {
            val ch = readNextValidChar()
            if (ch == '\\') {
                val ctl = readNextValidChar()
                if (ctl == 'u') {
                    val str = unicodeToString()
                    builder.append(str)
                }
                else if (!jsonControlMap.containsKey(ctl)) {
                    throw InvalidJSONFormatException("$ctl is not valid control char")
                }
                else {
                    builder.append(jsonControlMap[ctl])
                }
            }
            else if (ch == '\"') {
                break
            }
            else {
                builder.append(ch)
            }
        } while (true)

        readNextChar()
        return builder.toString()
    }

    private fun unicodeToString(): String {
        var buf = CharArray(4)
        readNextValidChars(buf)

        val str = String(buf)
        val code = Integer.valueOf(str, 16)

        val builder = StringBuilder()
        builder.appendCodePoint(code)
        return builder.toString()
    }

    private fun readNextValidChars(buf: CharArray, start: Int = 0) {
        if (buf.size - start < 0) {
            throw InvalidArgumentException(arrayOf("buf"))
        }

        for (i in start until buf.size) {
            buf[i] = readNextValidChar()
        }
    }

    private fun readNextValidChar(): Char {
        val ch = readNextChar()
        if (ch == -1) {
            throw IOException("Unexpect End")
        }

        return lastReadValidChar
    }

    fun readNextChar(): Int {
        val ch = reader.read()
        if (ch != -1) {
            lastReadValidChar = ch.toChar()
        }

        return ch
    }

    fun readFirstUnspaceChar(): Char {
        do {
            if (!isSpace(currentChar)) {
                return currentChar
            }

            readNextValidChar()
        } while (true)
    }

    fun readNextUnspaceChar() : Char {
        do {
            val ch = readNextValidChar()
            if (!isSpace(ch)) {
                return ch
            }
        } while (true)
    }
}