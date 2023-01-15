package com.tech4bytes.mbrosv3.Utils.centralCache

import android.text.format.DateUtils

class CacheModel: java.io.Serializable {
    var entryTime: Long = 0
    var expiryTime: Long = 0
    var content: Any

    constructor(content: Any) {
        entryTime = DateUtils.SECOND_IN_MILLIS
        expiryTime = DateUtils.SECOND_IN_MILLIS
        this.content = content
    }
}