package com.prasunmondal.postjsontosheets.clients.commons

class JSONUtils {
    companion object {
        fun jsonStringCleanUp(p2: String): String {
            var p3 = p2.replace("\"\\\"", "\"")
            p3 = p3.replace("\\\"\"", "\"")

            // handle lists
            p3 = p3.replace("\":\"[\\\"", "\":[\"")
            p3 = p3.replace("\\\",\\\"", "\",\"")
            p3 = p3.replace("\\\"]\"", "\"]")

            // handle maps
            p3 = p3.replace("\":\"{\\\"", "\":{\"")
            p3 = p3.replace("\\\":\\\"", "\":\"")
            p3 = p3.replace("\\\"}\"", "\"}")
            return p3
        }
    }
}