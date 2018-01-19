import org.libkt.json.JSONObject

fun main(args: Array<String>) {
    val str = """{
            "nullKey": null,
            "trueKey": true,
            "falseKey": false,
            "numberKey": 123.01,
            "stringKey": "Hello \u002aWorld",
            "objectKey": {
                "key1": "hello\tworld",
                "key2": ""
            },
            "arrayKey": [
                null,
                true,
                false,
                0.03,
                ""
            ]
        }"""
    val obj = JSONObject(str)
    println(obj)
}