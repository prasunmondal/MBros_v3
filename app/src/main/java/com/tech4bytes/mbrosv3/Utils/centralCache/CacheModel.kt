package com.tech4bytes.mbrosv3.Utils.centralCache

import android.text.format.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.time.LocalDateTime

class CacheModel: java.io.Serializable {
    var entryTime: LocalDateTime
    var expiryTime: LocalDateTime
    var content: Any

    constructor(content: Any) {
        entryTime = LocalDateTime.now()
        expiryTime = entryTime.plusMinutes(1)
        this.content = content
        LogMe.log(this.toString())
    }

    override fun toString(): String {
        return "CacheModel(entryTime=$entryTime, expiryTime=$expiryTime, content=$content)"
    }
}