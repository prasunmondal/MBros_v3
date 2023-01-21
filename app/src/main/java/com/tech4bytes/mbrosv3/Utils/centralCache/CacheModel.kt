package com.tech4bytes.mbrosv3.Utils.centralCache

import android.util.Log
import android.widget.Toast
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.time.LocalDateTime

class CacheModel: java.io.Serializable {
    var entryTime: LocalDateTime
    var expiryTime: LocalDateTime
    var content: Any?

    constructor(content: Any?) {
        entryTime = LocalDateTime.now()
        expiryTime = DateUtils.getNextTimeOccurrenceTimestamp(16)
//        Toast.makeText(AppContexts.get(), "$entryTime - $expiryTime", Toast.LENGTH_SHORT).show()
        LogMe.log("$entryTime - $expiryTime")
        this.content = content
        LogMe.log(this.toString())
    }

    override fun toString(): String {
        return "CacheModel(entryTime=$entryTime, expiryTime=$expiryTime, content=$content)"
    }
}