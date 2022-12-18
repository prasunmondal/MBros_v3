package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

data class GetCustomerOrders(var id: String = "",
                             var timestamp: String = "",
                             var name: String = "",
                             var seqNo: String = "",
                             var estimatePc: String = "",
                             var estimateKg: String = "",
                             var rate: String = "",
                             var due: String = ""): java.io.Serializable {


    companion object {

        fun get(useCache: Boolean = true): List<GetCustomerOrders> {
            val cacheResults = CentralCache.get<ArrayList<GetCustomerOrders>>(AppContexts.get(), CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, resultFromServer)
                resultFromServer
            }
        }

        fun getByName(inputName: String): GetCustomerOrders? {
            get().forEach {
                if(it.name == inputName) {
                    return it
                }
            }
            return null
        }

        fun save(objects: List<GetCustomerOrders>) {
            objects.forEach {
                it.timestamp = DateUtils.getCurrentTimestamp()
            }
            saveObjectsToServer(objects)
            saveToLocal(objects)
        }

        fun deleteAll() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
            saveToLocal(listOf())
        }

        private fun saveObjectsToServer(objects: List<GetCustomerOrders>) {
            objects.forEach {
                PostObject.builder()
                    .scriptId(ProjectConfig.dBServerScriptURL)
                    .sheetId(ProjectConfig.DB_SHEET_ID)
                    .tabName(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                    .dataObject(it as Any)
                    .build().execute()
            }
        }

        private fun saveToLocal(objects: List<GetCustomerOrders>) {
            CentralCache.put(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, objects)
        }

        private fun getFromServer(): List<GetCustomerOrders> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()

            return result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<GetCustomerOrders>?>() {}.type
            )
        }
    }

    override fun toString(): String {
        return "GetOrdersModel(id='$id', name='$name', seqNo='$seqNo', estimatePc='$estimatePc', estimateKg='$estimateKg', rate='$rate', due='$due')"
    }
}