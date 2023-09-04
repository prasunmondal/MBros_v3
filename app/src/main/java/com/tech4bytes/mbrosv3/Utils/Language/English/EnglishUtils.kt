package com.tech4bytes.mbrosv3.Utils.Language.English

class EnglishUtils {

    companion object {
        fun toWordCase(givenString: String): String {
            val arr = givenString.split(" ").toTypedArray()
            val sb = StringBuffer()
            for (i in arr.indices) {
                sb.append(arr[i][0].uppercaseChar())
                    .append(arr[i].substring(1)).append(" ")
            }
            return sb.toString().trim { it <= ' ' }
        }
    }
}