package com.tech4bytes.mbrosv3.Summary.DaySummary

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientSort
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.BusinessData.DayMetadata
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.ProjectConfig
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
    var police: String = "",
    var police_breakdown: String = "") : java.io.Serializable

object DaySummaryUtils: GSheetSerialized<DaySummary>(
    context = ContextWrapper(AppContexts.get()),
    sheetId = ProjectConfig.get_db_finalize_sheet_id(),
    tabName = "cumilativeCalculations",
    modelClass = DaySummary::class.java,
    sort = ClientSort("sortedByTimestamp") {list: List<DaySummary> -> ListUtils.sortListByAttribute(list, DaySummary::timestamp)}
) {
    fun getDaySummaryObjectForCurrentData(): DaySummary {
        val daySummaryObj = DaySummary()
        val metadata = DayMetadata.getRecords()

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
        daySummaryObj.prev_due_balance = "${DeliveryCalculations.getPrevCumilativeKhataDue()}"
        daySummaryObj.new_due_balance = "${DeliveryCalculations.getCumilativeKhataDue()}"
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
        daySummaryObj.police = metadata.police
        daySummaryObj.police_breakdown = metadata.police_breakdown

        return daySummaryObj
    }

    fun getPrevTripEndKm(useCache: Boolean = true): Int {
        val list = DaySummaryUtils.fetchAll(useCache).execute()
        return NumberUtils.getIntOrZero(list[list.size - 1].trip_end_km)
    }

    fun taxRate(): Double {
        return .001
    }

    fun getBirdCost(): Int {
        return (NumberUtils.getDoubleOrZero(DayMetadata.getRecords().actualLoadKg)
                * NumberUtils.getIntOrZero(DayMetadata.getRecords().finalFarmRate))
            .toInt()
    }

    fun kmCost(): Int {
        return (NumberUtils.getIntOrZero(DayMetadata.getRecords().vehicle_finalKm) - getPrevTripEndKm()) *
                NumberUtils.getIntOrZero(AppConstants.get(AppConstants.CAR_RATE_PER_KM))
    }

    fun getLabourCost(): Int {
        return NumberUtils.getIntOrZero(DayMetadata.getRecords().labour_expenses)
    }

    fun getExtraCost(): Int {
        return NumberUtils.getIntOrZero(DayMetadata.getRecords().extra_expenses)
    }

    fun getDaySale(): Int {
        return DeliveryCalculations.getDaySaleAmount()
    }

    fun getDayProfit(daySale: Int? = null): Int {
        // Sale - birdCost - kmCost - labCost - extraCost
        if(daySale == null) {
            return getDaySale() - getBirdCost() - kmCost() - getLabourCost() - getExtraCost()
        }
        return daySale - getBirdCost() - kmCost() - getLabourCost() - getExtraCost()
    }

    fun showDayProfit(): String {
        return if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_PROFITS)) {
                getDayProfit().toString()
        } else {
            LogMe.log("Showing Profit: Expected Permission: SHOW_PROFITS <PERMISSION DENIED>")
            "Sunshine"
        }
    }

    fun isDayFinalized(useCache: Boolean = true): Boolean {
        val bufferKm = NumberUtils.getIntOrZero(DayMetadata.getRecords(useCache).vehicle_finalKm)
        val lastFinalizedKm = getPrevTripEndKm(useCache)
        return (lastFinalizedKm == bufferKm || bufferKm == 0)
    }
}

