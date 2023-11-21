package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class DeliverToCustomerDataHandler {

    companion object {
        const val TAB_NAME = "DeliverOrders"
        // Fetch Op
        fun get(useCache: Boolean = true): List<DeliverToCustomerDataModel> {
            LogMe.log("Getting delivery data: tryCache: $useCache")
            val cacheResults = CentralCache.get<List<DeliverToCustomerDataModel>>(AppContexts.get(), TAB_NAME, useCache)

            if (cacheResults == null) {
                LogMe.log("Getting delivery data: Cache failed")
                parseAndSaveToLocal(TAB_NAME, getFromServer())
            }
            if (cacheResults == null) return listOf()
            return cacheResults
        }

        private fun getFromServer(): GetResponse {
            LogMe.log("Getting delivery data: Getting from server")
            return Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(TAB_NAME)
                .build().execute()
        }

        // Save Op
        fun save(obj: DeliverToCustomerDataModel, saveToLocal: Boolean) {
            LogMe.log("Getting delivery data: Save")
            obj.id = System.currentTimeMillis().toString()
            obj.date = DateUtils.getCurrentTimestamp()
            if (saveToLocal) {
                saveToLocal(obj)
            }
            saveToServer(obj)
        }

        fun saveToLocal(obj: DeliverToCustomerDataModel) {
            LogMe.log("Getting delivery data: Save To Local")
            val list = get() + obj
            CentralCache.put(TAB_NAME, list)
        }

        private fun saveToLocal(list: List<DeliverToCustomerDataModel>) {
            LogMe.log("Getting delivery data: Save To Local")
            CentralCache.put(TAB_NAME, list)
        }

        private fun <T> saveToServer(obj: T) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(TAB_NAME)
                .dataObject(obj as Any)
                .build().execute()
        }

        // Delete Data
        fun deleteAllData() {
            deleteAllFromServer()
            deleteAllFromLocal()
        }

        private fun deleteAllFromLocal() {
            CentralCache.put(TAB_NAME, listOf<DeliverToCustomerDataHandler>())
        }

        internal fun deleteAllFromServer() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(TAB_NAME)
                .build().execute()
        }

        fun parseToObject(result: GetResponse): List<DeliverToCustomerDataModel> {
            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<DeliverToCustomerDataModel>?>() {}.type)
        }

        fun parseAndSaveToLocal(cacheKey: String, result: GetResponse): List<DeliverToCustomerDataModel> {
            val parsedObj = parseToObject(result)
            val filteredResults = DeliverToCustomerCalculations.filterToOnlyLatest(parsedObj)
            CentralCache.put(cacheKey, filteredResults)
            return parsedObj
        }
    }
}