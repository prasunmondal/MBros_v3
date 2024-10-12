package com.tech4bytes.mbrosv3.BusinessData

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.device.DeviceUtils
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientFilter
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

object SingleAttributedDataUtils : GSheetSerialized<SingleAttributedDataModel>(
    ContextWrapper(AppContexts.get()),
    ProjectConfig.dBServerScriptURLNew,
    "1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w",
    "metadata",
    query = null,
    modelClass = SingleAttributedDataModel::class.java,
    filter = ClientFilter("latestRecordById") { list: List<SingleAttributedDataModel> -> arrayListOf(list.maxBy { (it).id }) }) {

    var globalObject: SingleAttributedDataModel? = null
    fun getRecords(useCache: Boolean = true): SingleAttributedDataModel {
        if(globalObject != null)
            return globalObject!!

        globalObject = fetchAll(useCache).execute()[0]
        return globalObject!!
    }

    fun saveToLocal(obj: SingleAttributedDataModel?) {
        if (obj != null) {
            obj.id = System.currentTimeMillis().toString()
            obj.recordGeneratorDevice = DeviceUtils.getUniqueID(AppContexts.get())
            obj.date = DateUtils.getCurrentTimestamp()
            globalObject = obj
        }
    }

    fun getBufferRateInt(): Int {
        if (getRecords().bufferRate.isEmpty())
            return 0
        return getRecords().bufferRate.toInt()
    }

    fun getFinalRateInt(): Int {
        if (getRecords().finalFarmRate.isEmpty())
            return 0
        return getRecords().finalFarmRate.toInt()
    }

    fun getExtraExpenseExcludingPolice(obj: SingleAttributedDataModel): Int {
        return NumberUtils.getIntOrZero(obj.extra_expenses) - NumberUtils.getIntOrZero(obj.police)
    }

    fun isCurrentDayRecord(): Boolean {
        val todaysDate1 = DateUtils.getCurrentDate("yyyy-dd-MM 00:00:00")
        val todaysDate2 = DateUtils.getCurrentDate("dd/MM/yyyy")
        val lastDate = DateUtils.getDateString(getRecords().datetime)
        return (lastDate == todaysDate1 || lastDate == todaysDate2)
    }

    fun getEstimatedSalary(): Int {
        var salaries = getRecords().salaryDivision.split("#")
        var total = 0
        salaries.forEach {
            total += NumberUtils.getIntOrZero(it.trim())
        }
        return total
    }

    fun resetForNextDay(obj: SingleAttributedDataModel): SingleAttributedDataModel {
        obj.labour_expenses = "0"
        obj.extra_expenses = "0"
        obj.police = "0"
        obj.police_breakdown = ""
        obj.actualLoadPc = "0"
        obj.actualLoadKg = "0"
        return obj
    }
}