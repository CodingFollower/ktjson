package org.stuff.ktjson

import com.sun.javaws.exceptions.InvalidArgumentException
import java.io.*
import java.nio.charset.Charset

internal val jsonControlMap = mapOf('\"' to '\"', '\\' to '\\', '/' to '/',
        'b' to '\b', 'f' to (12).toChar(),
        'n' to '\n', 'r' to '\r', 't' to '\t')
internal val controlToCharMap = mapOf('\"' to '\"', '\\' to '\\', '/' to '/',
        '\b' to 'b', (12).toChar() to 'f',
        '\n' to 'n', '\r' to 'r', '\t' to 't')
private val jsonValueEncloseChars = arrayOf('}', ']', ',')

private fun isSpace(ch: Char) : Boolean {
    return ch == ' '
            || ch == '\t'
            || ch == '\r'
            || ch == '\n'
}

internal class JSONInputStreamReader(stream : InputStream, private val charset: Charset = Charsets.UTF_8) {
    private val reader = InputStreamReader(stream, charset)
    private var lastReadValidChar: Char = (0).toChar()

    private val currentChar: Char
        get() {
            if (lastReadValidChar == (0).toChar()) {
                readNextValidChar()
            }

            return lastReadValidChar
        }

    internal constructor(text: String, charset: Charset = Charsets.UTF_8)
            : this(ByteArrayInputStream(text.toByteArray(charset)), charset)

    internal fun readJSONValue(): JSONValue {
        val ch = readFirstUnspaceChar()
        when(ch) {
            '{' -> return JSONObject(this, true)
            '[' -> return JSONArray(this, true)
            else -> return JSONPrimitiveValue(this, true)
        }
    }

    fun readNextValidValue(): String {
        val builder = StringBuilder()
        readFirstUnspaceChar()
        do {
            val ch = currentChar
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
        val buf = CharArray(4)
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
            throw InvalidJSONFormatException("Unexpect End")
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

    fun isLeftContainsUnspace() : Boolean {
        do {
            val ch = readNextChar()
            if (ch == -1) {
                return false
            }

            if (!isSpace(ch.toChar())) {
                return true
            }
        } while (true)
    }
}