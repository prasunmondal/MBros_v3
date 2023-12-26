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
    @Transient var getEmptyListIfEmpty: Boolean

    constructor(scriptURL: String, sheetURL: String, tabname: String, cacheObjectType: Type, appendInServer: Boolean, appendInLocal: Boolean, getEmptyListIfNoResultsFoundInServer: Boolean = false) {
        this.scriptURL = scriptURL
        this.sheetURL = sheetURL
        this.tabname = tabname
        this.cacheObjectType = cacheObjectType
        this.appendInServer = appendInServer
        this.appendInLocal = appendInLocal
        this.getEmptyListIfEmpty = getEmptyListIfNoResultsFoundInServer
    }

    fun <T : Any> get(useCache: Boolean = true, getEmptyListIfEmpty: Boolean = false, filterName: String = "default"): List<T> {
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
            var dataList = getFromServer<T>()
            if((getEmptyListIfEmpty || this.getEmptyListIfEmpty) && dataList.isEmpty())
                return listOf()
            dataList = filterResults(dataList)
            dataList = sortResults(dataList)

            CentralCache.put(cacheKey, dataList)
            dataList
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
        var callerClassName = ClassDetailsUtils.getCaller(ClassDetailsUtils.getCaller())
        return "$sheetURL/$tabname/$filterName"
    }

    open fun <T: Any> filterResults(list: ArrayList<T>): ArrayList<T> {
        return list
    }

    open fun <T: Any> sortResults(list: ArrayList<T>): ArrayList<T> {
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
    fun <T : Any> saveToServerThenLocal(dataObject: T) {
        saveToServer(dataObject)
        saveToLocal(dataObject)
    }

    /**
     * dataObject: Data to save
     * cacheKey: cacheKey used to identify the cache object, pass null to generate the cacheKey
     */
    fun <T : Any> saveToLocal(dataObject: T?, cacheKey: String? = getFilterName()) {
        var finalCacheKey = cacheKey
        if(finalCacheKey == null) {
            finalCacheKey = getFilterName()
        }
        if(dataObject == null) {
            CentralCache.put(finalCacheKey, dataObject)
            return
        }

        val dataToSave = if(appendInLocal) {
            var dataList = get<T>() as ArrayList
            dataList.addAll(arrayListOf(dataObject))
            dataList = filterResults(dataList)
            dataList = sortResults(dataList)
            dataList
        }
        else {
            dataObject
        }
        CentralCache.put(finalCacheKey, dataToSave)
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