package com.tech4bytes.mbrosv3.AppData

import android.os.Build
import androidx.annotation.RequiresApi
import com.prasunmondal.postjsontosheets.clients.commons.APIResponse
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.extrack.centralCache.utils.ClassDetailsUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.lang.reflect.Type


abstract class Tech4BytesSerializable<T : Any> : java.io.Serializable {

    @Transient
    var scriptURL: String

    @Transient
    var sheetURL: String

    @Transient
    var tabname: String

    @Transient
    var cacheObjectType: Type

    @Transient
    var appendInServer: Boolean

    @Transient
    var appendInLocal: Boolean

    @Transient
    var getEmptyListIfEmpty: Boolean

    constructor(scriptURL: String, sheetURL: String, tabname: String, cacheObjectType: Type, appendInServer: Boolean, appendInLocal: Boolean, getEmptyListIfNoResultsFoundInServer: Boolean = false) {
        this.scriptURL = scriptURL
        this.sheetURL = sheetURL
        this.tabname = tabname
        this.cacheObjectType = cacheObjectType
        this.appendInServer = appendInServer
        this.appendInLocal = appendInLocal
        this.getEmptyListIfEmpty = getEmptyListIfNoResultsFoundInServer
    }

    @RequiresApi(34)
    fun get(useCache: Boolean = true, getEmptyListIfEmpty: Boolean = false, filterName: String = "default"): List<T> {
        val cacheKey = getFilterName(filterName)
        LogMe.log("Getting records: " + cacheKey)
        val cacheResults = try {
            CentralCache.get<T>(AppContexts.get(), cacheKey, useCache)
        } catch (ex: ClassCastException) {
            arrayListOf(CentralCache.get<T>(AppContexts.get(), cacheKey, useCache))
        }

        LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
        return if (cacheResults != null) {
            cacheResults as List<T>
        } else {
            val parsedResponse = parseNGetResponse(getFromServer())
            CentralCache.put(cacheKey, parsedResponse)
            parsedResponse
        }
    }

    fun parseNGetResponse(rawResponse: GetResponse): List<T> {
        var parsedResponse = rawResponse.parseToObject<T>(rawResponse.getRawResponse(), cacheObjectType)
        if ((getEmptyListIfEmpty || this.getEmptyListIfEmpty) && parsedResponse.isEmpty())
            return listOf()
        parsedResponse = filterResults(parsedResponse)
        parsedResponse = sortResults(parsedResponse)
        return parsedResponse
    }

    fun isDataAvailable(filterName: String = "default"): Boolean {
        val useCache = true
        val cacheKey = getFilterName(filterName)
        LogMe.log("Getting records: " + cacheKey)
        val cacheResults = try {
            CentralCache.get<T>(AppContexts.get(), cacheKey, useCache)
        } catch (ex: ClassCastException) {
            arrayListOf(CentralCache.get<T>(AppContexts.get(), cacheKey, useCache))
        }
        return cacheResults != null
    }

    private fun getFromServer(): GetResponse {
        LogMe.log("Expensive Operation - get data from server: $sheetURL - $tabname")
        val result: GetResponse = Get.builder()
            .scriptId(scriptURL)
            .sheetId(sheetURL)
            .tabName(tabname)
            .build().execute()

        return result
    }

    fun parseAndSaveToCache(response: GetResponse) {
        val cacheKey = getFilterName()
        val parsedData = parseNGetResponse(response)
        CentralCache.put(cacheKey, parsedData)
        LogMe.log("Put Complete")
        LogMe.log("filterName: $cacheKey")
//        parsedData.forEach {
//            LogMe.log(it.toString())
//        }
    }

    private fun getFilterName(filterName: String = "default"): String {
//        var callerClassName = ClassDetailsUtils.getCaller(ClassDetailsUtils.getCaller())
        return "$sheetURL/$tabname/$filterName"
    }

    open fun <T : Any> filterResults(list: ArrayList<T>): ArrayList<T> {
        return list
    }

    open fun <T : Any> sortResults(list: ArrayList<T>): ArrayList<T> {
        return list
    }

    fun <T : Any> saveToLocalThenServer(dataObject: T) {
        saveToLocal(dataObject)
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
    fun saveToServerThenLocal(dataObject: T) {
        saveToServer(dataObject)
        saveToLocal(dataObject)
    }

    /**
     * dataObject: Data to save
     * cacheKey: cacheKey used to identify the cache object, pass null to generate the cacheKey
     */
    fun saveToLocal(dataObject: Any?, cacheKey: String? = getFilterName()) {
        var finalCacheKey = cacheKey
        if(cacheKey == null) {
            finalCacheKey = getFilterName()
        }
        LogMe.log("Expensive Operation - saving data to local: $finalCacheKey")
        if (finalCacheKey == null) {
            finalCacheKey = getFilterName()
        }
        if (dataObject == null) {
            CentralCache.put(finalCacheKey, dataObject)
            return
        }

        val dataToSave = if (appendInLocal) {
            var dataList = get() as ArrayList
            dataList.addAll(arrayListOf(dataObject as T))
            dataList = filterResults(dataList)
            dataList = sortResults(dataList)
            dataList
        } else {
            dataObject
        }
        CentralCache.put(finalCacheKey, dataToSave)
    }

    fun <T> saveToServer(obj: T) {
        LogMe.log("Expensive Operation - saving data to server: $sheetURL - $tabname")
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