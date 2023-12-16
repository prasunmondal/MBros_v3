package com.tech4bytes.mbrosv3.AppData

import com.prasunmondal.postjsontosheets.clients.delete.Delete
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
    @Transient var appendInServer: Boolean
    @Transient var appendInLocal: Boolean

    constructor(scriptURL: String, sheetURL: String, tabname: String, cacheObjectType: Type, appendInServer: Boolean, appendInLocal: Boolean) {
        this.scriptURL = scriptURL
        this.sheetURL = sheetURL
        this.tabname = tabname
        this.appendInServer = appendInServer
        this.appendInLocal = appendInLocal
        this.cacheObjectType = cacheObjectType
    }

    fun <T> get(useCache: Boolean = true, filterName: String = "default"): List<T> {
        val cacheKey = getFilterName(filterName)
        LogMe.log("Getting records")

        val cacheResults = try {
            CentralCache.get<ArrayList<T>>(AppContexts.get(), cacheKey, useCache)
        } catch (ex: ClassCastException) {
            arrayListOf(CentralCache.get<T>(AppContexts.get(), cacheKey, useCache))
        }

        LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
        return if (cacheResults != null) {
            cacheResults as ArrayList<T>
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

    fun <T : Any> saveToLocalThenServer(dataObject: T) {
        saveToLocal(dataObject, getFilterName())
        saveToServer(dataObject)
    }

    /*
    *
    *
    *
    * Save Data Code
    *
    *
    *
     */
    fun <T : Any> saveToServerThenLocal(dataObject: T) {
        saveToServer(dataObject)
        saveToLocal(dataObject, getFilterName())
    }

    fun <T : Any> saveToLocal(dataObject: T?, cacheKey: String = getFilterName()) {
        if(dataObject == null) {
            CentralCache.put(cacheKey, dataObject)
            return
        }

        val dataToSave = if(appendInLocal) {
            val allData = get<T>() as MutableList
            allData.addAll(arrayListOf(dataObject))
            allData
        }
        else {
            dataObject
        }
        CentralCache.put(cacheKey, dataToSave)
    }

    fun <T> saveToServer(obj: T) {
        if (!appendInServer) {
            deleteDataFromServer()
        }

        PostObject.builder()
            .scriptId(scriptURL)
            .sheetId(sheetURL)
            .tabName(tabname)
            .dataObject(obj as Any)
            .build().execute()
    }


    /*
    *
    *
    *
    *
    * Deletion Codes
    *
    *
    *
     */

    fun deleteData() {
        deleteDataFromServer()
        deleteDataFromLocal()
    }

    fun deleteDataFromLocal() {
        saveToLocal(null)
    }

    fun deleteDataFromServer() {
        Delete.builder()
            .scriptId(scriptURL)
            .sheetId(sheetURL)
            .tabName(tabname)
            .build().execute()
    }
}