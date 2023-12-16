package com.tech4bytes.mbrosv3.AppData

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.ProjectConfig

class modelClass: java.io.Serializable, Tech4BytesSerializable(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "testAnd",
    object : TypeToken<ArrayList<modelClass?>?>() {}.type
) {
    var t1: String = ""
    var t2: String = ""
}