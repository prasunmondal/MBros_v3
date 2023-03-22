package com.tech4bytes.mbrosv3.Summary.DaySummary

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverOrdersConfig
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Summary.SummaryConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

data class DaySummary(var timestamp: String = "",
                      var date: String = "",
                      var pc: String = "",
                      var kg: String = "",
                      var avgLoadingWt: String = "",
                      var shortage: String = "",
                      var sold_amount: String = "",
                      var paid_amount: String = "",
                      var prev_due_balance: String = "",
                      var new_due_balance: String = "",
                      var loadingCompany: String = "",
                      var loadingBranch: String = "",
                      var loadingArea: String = "",
                      var loadingComapnyAccount: String = "",
                      var loadingCompanyAccountBalance: String = "",
                      var car_expense: String = "",
                      var labour_expense: String = "",
                      var extra_expenses: String = "",
                      var total_expenses: String = "",
                      var farm_rate: String = "",
                      var buffer: String = "",
                      var final_km_reading: String = "",
                      var transport_income: String = "",
                      var transport_expenses: String = "",
                      var profit: String = "",
                      var trip_end_km: String = ""): java.io.Serializable {

    companion object {
        fun get(useCache: Boolean = true): List<DaySummary> {
            var cacheResults = CentralCache.get<List<DaySummary>>(AppContexts.get(), SummaryConfig.TAB_DAY_SUMMARY, useCache)

            if (cacheResults == null) {
                cacheResults = getFromServer()
                CentralCache.put(SummaryConfig.TAB_DAY_SUMMARY, cacheResults)
            }
            return cacheResults
        }

        private fun getFromServer(): List<DaySummary> {
            // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(SummaryConfig.DB_FINALIZE_SHEET_ID)
                .tabName(SummaryConfig.TAB_DAY_SUMMARY)
                .build().execute()

            // waitDialog!!.dismiss()
            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<DaySummary>?>() {}.type
            )
        }
    }
}
