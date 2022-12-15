package com.tech4bytes.mbrosv3.GetOrders

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

data class OrderEstimateModel(var id: String = "",
                              var name: String = "",
                              var seqNo: String = "",
                              var estimatePc: String = "",
                              var estimateKg: String = "",
                              var rate: String = "",
                              var due: String = ""): java.io.Serializable {

    override fun toString(): String {
        return "OrderEstimateModel(id='$id', name='$name', estimatePc='$estimatePc', rate='$rate', due='$due', estimateKg='$estimateKg')"
    }

    companion object {

        val cacheKey = "allCustomersList"

        fun get(useCache: Boolean = true): List<OrderEstimateModel> {
            val cacheResults = CentralCache.get<ArrayList<OrderEstimateModel>>(AppContexts.get(), GetOrdersConfig.CACHE_KEY__ORDERS, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(cacheKey, resultFromServer)
                resultFromServer
            }
        }

        fun save(objects: List<OrderEstimateModel>) {
            saveObjectsToServer(objects)
            saveToLocal(objects)
        }

        fun deleteAll() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(GetOrdersConfig.SHEET_TAB_NAME)
                .build().execute()
        }

        private fun saveObjectsToServer(objects: List<OrderEstimateModel>) {
            objects.forEach {
                PostObject.builder()
                    .scriptId(ProjectConfig.dBServerScriptURL)
                    .sheetId(ProjectConfig.DB_SHEET_ID)
                    .tabName(GetOrdersConfig.SHEET_TAB_NAME)
                    .dataObject(it as Any)
                    .build().execute()
            }
        }

        private fun saveToLocal(objects: List<OrderEstimateModel>) {
            CentralCache.put(cacheKey, objects)
        }

        private fun getFromServer(): List<OrderEstimateModel> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(GetOrdersConfig.SHEET_TAB_NAME)
                .build().execute()

            return result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<OrderEstimateModel>?>() {}.type
            )
        }
    }
}