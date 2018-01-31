package org.stuff.ktjson

class KeyNotFoundException(key: String) : Exception("\"$key\" not found")