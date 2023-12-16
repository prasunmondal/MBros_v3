package com.tech4bytes.mbrosv3.AppData

import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.extrack.centralCache.utils.ClassDetailsUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.lang.reflect.Type


abstract class Tech4BytesSerializable : java.io.Serializable {

    @Transient var scriptURL: String
    @Transient var sheetURL: String
    @Transient var tabname: String
    @Transient var cacheObjectType: Type

    constructor(scriptURL: String, sheetURL: String, tabname: String, cacheObjectType: Type) {
        this.scriptURL = scriptURL
        this.sheetURL = sheetURL
        this.tabname = tabname
        this.cacheObjectType = cacheObjectType
    }

    fun <T> get(useCache: Boolean = true, filterName: String = "default"): ArrayList<T> {
        val cacheKey = getFilterName(filterName)
        LogMe.log("Getting records")
        val cacheResults = CentralCache.get<ArrayList<T>>(AppContexts.get(), cacheKey, useCache)
        LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
        return if (cacheResults != null) {
            cacheResults
        } else {
            val resultFromServer = getFromServer<T>()
            CentralCache.put(cacheKey, resultFromServer)
            resultFromServer
        }
    }

    private fun <T> getFromServer(): ArrayList<T> {
        val result: GetResponse = Get.builder()
            .scriptId(scriptURL)
            .sheetId(sheetURL)
            .tabName(tabname)
            .build().execute()

        return result.parseToObject(result.getRawResponse(), cacheObjectType)
    }

    private fun getFilterName(filterName: String = "default"): String {
        return "${ClassDetailsUtils.getCaller(ClassDetailsUtils.getCaller())}/$sheetURL/$tabname/$filterName"
    }

    fun <T> saveToLocalThenServer(obj: T) {
        saveToLocal(obj, getFilterName())
//        saveToServer(obj)
    }

    fun <T> saveToServerThenLocal(obj: T) {
//        saveToServer(obj)
        saveToLocal(obj, getFilterName())
    }

    fun <T> saveToLocal(obj: T, cacheKey: String = getFilterName()) {
        CentralCache.put(cacheKey, obj)
    }

    fun saveToServer(obj: modelClass) {

        PostObject.builder()
            .scriptId(scriptURL)
            .sheetId(sheetURL)
            .tabName(tabname)
            .dataObject(obj as Any)
            .build().execute()
    }
}