package com.tech4bytes.mbrosv3.Customer

import android.app.ProgressDialog
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

data class CustomerKYCModel(var date: String,
                            var nameEng: String,
                            var nameBeng: String,
                            var phNo1: String,
                            var phNo2: String,
                            var address: String,
                            var showDue: String,
                            var rateDifference: String = "",
                            var isActiveCustomer: String = ""): java.io.Serializable {

    fun getDisplayName(): String {
        return this.nameEng
    }
}

class CustomerKYC: java.io.Serializable {
    companion object {

        fun getAllCustomers(useCache: Boolean = true): List<CustomerKYCModel> {
            val cacheKey = "allCustomersList"
            val cacheResults = CentralCache.get<ArrayList<CustomerKYCModel>>(AppContexts.get(), cacheKey, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getCustomerListFromServer()
                CentralCache.put(cacheKey, resultFromServer)
                resultFromServer
            }
        }

        fun get(englishName: String): CustomerKYCModel? {
            getAllCustomers().forEach {
                if (it.nameEng == englishName)
                    return it
            }
            return null
        }

        fun showBalance(engName: String): Boolean {
            getAllCustomers().forEach {
                if (it.nameEng == engName)
                    return it.showDue.toBoolean()
            }
            return true
        }

        fun getCustomerByEngName(engName: String): CustomerKYCModel? {
            getAllCustomers().forEach {
                if (it.nameEng == engName)
                    return it
            }
            return null
        }

        private fun getCustomerListFromServer(): List<CustomerKYCModel> {
            // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(Customer_Config.SHEET_TAB_NAME)
                .build().execute()

            // waitDialog!!.dismiss()
            return result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<CustomerKYCModel>?>() {}.type
            )
        }
    }
}