package com.tech4bytes.mbrosv3.Summary.DaySummary

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Summary.SummaryConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.Utils.WeightUtils.WeightUtils

data class DaySummary(
    var timestamp: String = "",
    var date: String = "",
    var pc: String = "",
    var kg: String = "",
    var deliveredPc: String = "",
    var deliveredKg: String = "",
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
    var transport_income: String = "",
    var transport_expenses: String = "",
    var profit: String = "",
    var trip_end_km: String = "",
    var other_expenses_note: String = "",
    var other_expense_amount: String = "",

    ) : java.io.Serializable {

    companion object {
        fun get(useCache: Boolean = true): List<DaySummary> {
            var cacheResults = CentralCache.get<List<DaySummary>>(AppContexts.get(), SummaryConfig.TAB_DAY_SUMMARY, useCache)

            if (cacheResults == null) {
                cacheResults = getFromServer()
                CentralCache.put(SummaryConfig.TAB_DAY_SUMMARY, cacheResults)
            }
            return cacheResults
        }

        fun saveToServer() {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_finalize_sheet_id())
                .tabName(SummaryConfig.TAB_DAY_SUMMARY)
                .dataObject(getDaySummaryObjectForCurrentData() as Any)
                .build().execute()
        }

        fun getDaySummaryObjectForCurrentData(): DaySummary {
            val daySummaryObj = DaySummary()
            val metadata = SingleAttributedData.getRecords()

            val loadAvgWt = NumberUtils.getDoubleOrZero(metadata.actualLoadKg) / NumberUtils.getDoubleOrZero(metadata.actualLoadPc)

            daySummaryObj.timestamp = System.currentTimeMillis().toString()
            daySummaryObj.date = DateUtils.getCurrentDate("yyyy-MM-dd")
            daySummaryObj.pc = metadata.actualLoadPc
            daySummaryObj.kg = metadata.actualLoadKg
            daySummaryObj.deliveredPc = DeliveryCalculations.getTotalDeliveredPc().toString()
            daySummaryObj.deliveredKg = DeliveryCalculations.getTotalDeliveredKg().toString()
            daySummaryObj.avgLoadingWt = "${NumberUtils.roundOff3places(WeightUtils.roundOff3places(loadAvgWt))}"
            daySummaryObj.shortage = "${NumberUtils.roundOff3places(DeliveryCalculations.getShortage(daySummaryObj.kg, daySummaryObj.deliveredKg))}"
            daySummaryObj.sold_amount = "${DeliveryCalculations.getDaySaleAmount()}"
            daySummaryObj.paid_amount = "${DeliveryCalculations.getTotalOfPaidAmounts()}"
            daySummaryObj.prev_due_balance = "${DeliveryCalculations.getPrevMarketDue()}"
            daySummaryObj.new_due_balance = "${DeliveryCalculations.getTotalMarketDue()}"
            daySummaryObj.loadingCompany = metadata.load_companyName
            daySummaryObj.loadingBranch = metadata.load_branch
            daySummaryObj.loadingArea = metadata.load_area
            daySummaryObj.loadingComapnyAccount = metadata.load_account
            daySummaryObj.loadingCompanyAccountBalance = ""
            daySummaryObj.car_expense = DeliveryCalculations.getKmCost().toString()
            daySummaryObj.labour_expense = metadata.labour_expenses
            daySummaryObj.extra_expenses = metadata.extra_expenses
            daySummaryObj.total_expenses = DeliveryCalculations.getTotalOtherExpenses().toString()
            daySummaryObj.farm_rate = metadata.finalFarmRate
            daySummaryObj.buffer = metadata.bufferRate
            daySummaryObj.trip_end_km = metadata.vehicle_finalKm
            daySummaryObj.transport_income = ""
            daySummaryObj.transport_expenses = ""
            daySummaryObj.other_expenses_note = ""
            daySummaryObj.other_expense_amount = ""
            daySummaryObj.profit = getDayProfit().toString()

            return daySummaryObj
        }

        private fun getFromServer(): List<DaySummary> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_finalize_sheet_id())
                .tabName(SummaryConfig.TAB_DAY_SUMMARY)
                .build().execute()

            val parsedResult = result.parseToObject<DaySummary>(
                result.getRawResponse(),
                object : TypeToken<ArrayList<DaySummary>?>() {}.type
            )

            return ListUtils.sortListByAttribute(parsedResult, DaySummary::timestamp)
        }

        fun getPrevTripEndKm(): Int {
            val list = get()
            return NumberUtils.getIntOrZero(list[list.size - 1].trip_end_km)
        }

        fun taxRate(): Double {
            return .001
        }

        fun getBirdCost(): Int {
            return (NumberUtils.getDoubleOrZero(SingleAttributedData.getRecords().actualLoadKg)
                    * NumberUtils.getIntOrZero(SingleAttributedData.getRecords().finalFarmRate))
                .toInt()
        }

        fun kmCost(): Int {
            return (NumberUtils.getIntOrZero(SingleAttributedData.getRecords().vehicle_finalKm) - getPrevTripEndKm()) * 12
        }

        fun getLabourCost(): Int {
            return NumberUtils.getIntOrZero(SingleAttributedData.getRecords().labour_expenses)
        }

        fun getExtraCost(): Int {
            return NumberUtils.getIntOrZero(SingleAttributedData.getRecords().extra_expenses)
        }

        fun getDaySale(): Int {
            return NumberUtils.getIntOrZero(SingleAttributedData.getRecords().daySale)
        }

        fun getDayProfit(): Int {
            // Sale - birdCost - kmCost - labCost - extraCost
            LogMe.log(getDaySale())
            LogMe.log(getBirdCost())
            LogMe.log(kmCost())
            LogMe.log(getLabourCost())
            LogMe.log(getExtraCost())
            return getDaySale() - getBirdCost() - kmCost() - getLabourCost() - getExtraCost()
        }

        fun showDayProfit(): String {
            return if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_PROFITS)) {
                LogMe.log("Showing Profit: " + getDayProfit())
                getDayProfit().toString()
            } else {
                LogMe.log("Showing Profit: Expected Permission: SHOW_PROFITS <PERMISSION DENIED>")
                "Sunshine"
            }
        }
    }
}
