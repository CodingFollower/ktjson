package org.stuff.ktjson.error

class InvalidJSONFormatException(val position: Long, msg: String) : Exception(msg)