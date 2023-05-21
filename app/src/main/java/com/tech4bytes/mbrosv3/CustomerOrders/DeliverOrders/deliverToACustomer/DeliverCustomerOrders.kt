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

data class DeliverCustomerOrders(
    var id: String = "",
    var date: String = "",
    var timestamp: String = "",
    var name: String = "",
    var orderedPc: String = "",
    var orderedKg: String = "",
    var deliveredPc: String = "",
    var deliveredKg: String = "",
    var rate: String = "",
    var todaysAmount: String = "",
    var prevDue: String = "",
    var totalDue: String = "",
    var paid: String = "",
    var balanceDue: String = "",
    var deliveryStatus: String = ""
) : java.io.Serializable {

    companion object {

        // Fetch Op
        fun get(useCache: Boolean = true): List<DeliverCustomerOrders> {
            LogMe.log("Getting delivery data: tryCache: $useCache")
            var cacheResults = CentralCache.get<List<DeliverCustomerOrders>>(AppContexts.get(), DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            if (cacheResults == null) {
                LogMe.log("Getting delivery data: Cache failed")
                cacheResults = DeliveryCalculationUtils.filterToOnlyLatest(getFromServer())
                saveToLocal(cacheResults)
            }

            return cacheResults
        }

        private fun getFromServer(): List<DeliverCustomerOrders> {
            LogMe.log("Getting delivery data: Getting from server")
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()

            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<DeliverCustomerOrders>?>() {}.type
            )
        }

        // Save Op
        fun save(obj: DeliverCustomerOrders) {
            LogMe.log("Getting delivery data: Save")
            obj.id = System.currentTimeMillis().toString()
            obj.date = DateUtils.getCurrentTimestamp()
            saveToLocal(obj)
            saveToServer(obj)
        }

        private fun saveToLocal(obj: DeliverCustomerOrders) {
            LogMe.log("Getting delivery data: Save To Local")
            val list = get() + obj
            CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, list)
        }

        private fun saveToLocal(list: List<DeliverCustomerOrders>) {
            LogMe.log("Getting delivery data: Save To Local")
            CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, list)
        }

        private fun <T> saveToServer(obj: T) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .dataObject(obj as Any)
                .build().execute()
        }


        // Delete Data

        fun deleteAllData() {
            deleteAllFromServer()
            deleteAllFromLocal()
        }

        private fun deleteAllFromLocal() {
            CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, listOf<DeliverCustomerOrders>())
        }

        private fun deleteAllFromServer() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
        }
    }
}