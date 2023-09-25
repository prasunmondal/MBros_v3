package com.tech4bytes.mbrosv3.Utils.Language.English

import org.apache.commons.lang3.text.WordUtils

class EnglishUtils {

    companion object {
        fun toWordCase(text: String): String {
            return WordUtils.capitalize(text)
        }
    }
}