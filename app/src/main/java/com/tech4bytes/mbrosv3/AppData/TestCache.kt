package com.tech4bytes.mbrosv3.AppData

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.ProjectConfig
import java.lang.reflect.Type

//import javax.inject.Singleton

//@Singleton
//class TestCache: Tech4BytesSerializable() {
//
//}

class modelClass: Tech4BytesSerializable() {
    var t1: String = ""
    var t2: String = ""

    override var scriptURL: String = ProjectConfig.dBServerScriptURL
    override var sheetURL: String = ProjectConfig.get_db_sheet_id()
    override var tabname: String = "testAnd"
    override var cacheObjectType: Type = object : TypeToken<ArrayList<modelClass?>?>() {}.type
}