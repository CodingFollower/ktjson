package org.stuff.ktjson.error

import org.stuff.ktjson.JSONType

class TypeErrorException(msg: String = "") : Exception(msg) {
    internal constructor(expect: JSONType, actual: JSONType): this("Expect $expect, Actual $actual")
}