package com.tech4bytes.mbrosv3.AppData

import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.DB.clients.get.ByQuery.GetByQuery
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
    var query: String?

    @Transient
    var cacheObjectType: Type

    @Transient
    var appendInServer: Boolean

    @Transient
    var appendInLocal: Boolean

    @Transient
    var getEmptyListIfEmpty: Boolean

    @Transient
    var cacheTag: String = "default"

    constructor(
        scriptURL: String,
        sheetURL: String,
        tabname: String,
        query: String? = null,
        cacheObjectType: Type,
        appendInServer: Boolean,
        appendInLocal: Boolean,
        getEmptyListIfNoResultsFoundInServer: Boolean = false,
        cacheTag: String = "default") {
        this.scriptURL = scriptURL
        this.sheetURL = sheetURL
        this.tabname = tabname
        this.query = query
        this.cacheObjectType = cacheObjectType
        this.appendInServer = appendInServer
        this.appendInLocal = appendInLocal
        this.getEmptyListIfEmpty = getEmptyListIfNoResultsFoundInServer
        this.cacheTag = cacheTag
    }

    fun get(
        useCache: Boolean = true,
        getEmptyListIfEmpty: Boolean = false,
        cacheTag: String = this.cacheTag
    ): List<T> {
        val cacheKey = getFilterName(cacheTag)
        LogMe.log("Getting records: $cacheKey")
        val cacheResults = try {
            CentralCache.get<T>(AppContexts.get(), cacheKey, useCache)
        } catch (ex: ClassCastException) {
            arrayListOf(CentralCache.get<T>(AppContexts.get(), cacheKey, useCache))
        }

        LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
        return if (cacheResults != null) {
            cacheResults as List<T>
        } else {
            synchronized(Tech4BytesSerializableLocks.getLock(cacheKey)!!) {
                // Synchronized code block
                println("Synchronized function called with key: $cacheKey")
                parseAndSaveToCache(getFromServer(), cacheKey)
            }
        }
    }

    private fun parseNGetResponse(rawResponse: GetResponse): List<T> {
        var parsedResponse = rawResponse.parseToObject<T>(rawResponse.getRawResponse(), cacheObjectType)
        if ((getEmptyListIfEmpty || this.getEmptyListIfEmpty) && parsedResponse.isEmpty())
            return listOf()
        parsedResponse = filterResults(parsedResponse)
        parsedResponse = sortResults(parsedResponse)
        return parsedResponse
    }

    fun isDataAvailable(cacheTag: String = "default"): Boolean {
        val useCache = true
        val cacheKey = getFilterName(cacheTag)
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
        val result: GetResponse = if(query == null || query!!.isEmpty()) {
             Get.builder()
                .scriptId(scriptURL)
                .sheetId(sheetURL)
                .tabName(tabname)
                .build().execute()
        } else {
            GetByQuery.builder()
                .scriptId(scriptURL)
                .sheetId(sheetURL)
                .tabName(tabname)
                .query(query!!)
                .build().execute()
        }

        return result
    }

    fun parseAndSaveToCache(response: GetResponse, cacheKey: String? = null): List<T> {
        val resolvedCacheKey = if(cacheKey.isNullOrEmpty()) {
            getFilterName()
        } else {
            cacheKey
        }
        val parsedData = parseNGetResponse(response)
        CentralCache.put(resolvedCacheKey, parsedData)
        LogMe.log("Put Complete")
        LogMe.log("cacheKey: $resolvedCacheKey")
        return parsedData
    }

    private fun getFilterName(cacheTag: String = "default"): String {
//        var callerClassName = ClassDetailsUtils.getCaller(ClassDetailsUtils.getCaller())
        return "$sheetURL/$tabname/$cacheTag"
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