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

        // Fetch Op
        fun get(useCache: Boolean = true): List<DeliverToCustomerDataModel> {
            LogMe.log("Getting delivery data: tryCache: $useCache")
            var cacheResults = CentralCache.get<List<DeliverToCustomerDataModel>>(AppContexts.get(), DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            if (cacheResults == null) {
                LogMe.log("Getting delivery data: Cache failed")
                cacheResults = DeliverToCustomerCalculations.filterToOnlyLatest(getFromServer())
                saveToLocal(cacheResults)
            }

            return cacheResults
        }

        private fun getFromServer(): List<DeliverToCustomerDataModel> {
            LogMe.log("Getting delivery data: Getting from server")
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()

            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<DeliverToCustomerDataModel>?>() {}.type
            )
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
            CentralCache.put(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, list)
        }

        private fun saveToLocal(list: List<DeliverToCustomerDataModel>) {
            LogMe.log("Getting delivery data: Save To Local")
            CentralCache.put(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, list)
        }

        private fun <T> saveToServer(obj: T) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .dataObject(obj as Any)
                .build().execute()
        }


        // Delete Data
        fun deleteAllData() {
            deleteAllFromServer()
            deleteAllFromLocal()
        }

        private fun deleteAllFromLocal() {
            CentralCache.put(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, listOf<DeliverToCustomerDataHandler>())
        }

        private fun deleteAllFromServer() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
        }
    }
}