package com.tech4bytes.mbrosv3.AppData

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.ProjectConfig

class modelClass: java.io.Serializable {
    var t1: String = ""
    var t2: String = ""
}

object UtilClass: Tech4BytesSerializable(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "testAnd",
    appendInServer = true,
    appendInLocal = true,
    cacheObjectType = object : TypeToken<ArrayList<modelClass?>?>() {}.type
)