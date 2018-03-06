package org.stuff.ktjson.error

class KeyNotFoundException(key: String) : Exception("\"$key\" not found")