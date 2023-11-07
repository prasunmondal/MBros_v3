package com.tech4bytes.mbrosv3.Customer

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

data class CustomerKYCModel(
    var date: String,
    var nameEng: String,
    var nameBeng: String,
    var phNo1: String,
    var phNo2: String,
    var address: String,
    var showDue: String,
    var rateDifference: String = "",
    var isActiveCustomer: String = "",
    var showBalanceView: Boolean,
    var smsNumber: String = "",
    var smsText: String = "",
) : java.io.Serializable {

    fun getDisplayName(): String {
        return this.nameEng
    }
}

class CustomerKYC : java.io.Serializable {
    companion object {
        val SHEET_TAB_NAME = "customerDetails"
        val cacheKey = "customerDetails"

        fun getAllCustomers(useCache: Boolean = true): List<CustomerKYCModel> {
            val cacheResults = CentralCache.get<ArrayList<CustomerKYCModel>>(AppContexts.get(), cacheKey, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                parseAndSaveToLocal(cacheKey, resultFromServer)
            }
        }

        private fun getFromServer(): GetResponse {
            return Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(SHEET_TAB_NAME)
                .build().execute()
        }

        fun parseToObject(result: GetResponse): List<CustomerKYCModel> {
            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<CustomerKYCModel>?>() {}.type)
        }

        fun parseAndSaveToLocal(cacheKey: String, result: GetResponse): List<CustomerKYCModel> {
            val parsedObj = parseToObject(result)
            CentralCache.put(cacheKey, parsedObj)
            return parsedObj
        }
    }
}