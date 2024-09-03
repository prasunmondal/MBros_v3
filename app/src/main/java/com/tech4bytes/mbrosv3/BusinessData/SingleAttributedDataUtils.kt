package com.tech4bytes.mbrosv3.BusinessData

import com.prasunmondal.dev.libs.device.DeviceUtils
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientFilter
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1


object SingleAttributedDataUtils : GSheetSerialized<SingleAttributedDataModel>(
    ContextWrapper(AppContexts.get()),
    ProjectConfig.dBServerScriptURLNew,
    "1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w",
    "metadata",
    query = null,
    classTypeForResponseParsing = SingleAttributedDataModel::class.java,
    appendInServer = true,
    appendInLocal = true,
    filter = ClientFilter("latestRecordById") { list: List<SingleAttributedDataModel> -> arrayListOf(list.maxBy { (it).id }) }) {

    fun getRecords(useCache: Boolean = true): SingleAttributedDataModel {
        val t = fetchAll().execute(useCache)
        return t[0]
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

    fun saveAttributeToLocal(kMutableProperty: KMutableProperty1<SingleAttributedDataModel, String>, value: String) {
        val obj = getRecords()
        ReflectionUtils.setAttribute(obj, kMutableProperty, value)
        saveToLocal(obj)
    }

    fun saveToLocal(obj: SingleAttributedDataModel?) {
        if (obj != null) {
            obj.id = System.currentTimeMillis().toString()
            obj.recordGeneratorDevice = DeviceUtils.getUniqueID(AppContexts.get())
            obj.date = DateUtils.getCurrentTimestamp()
            insert(obj).saveToLocal(false)
        }
    }

    fun invalidateCache() {
        saveToLocal(null)
    }

    fun getExtraExpenseExcludingPolice(obj: SingleAttributedDataModel): Int {
        return NumberUtils.getIntOrZero(obj.extra_expenses) - NumberUtils.getIntOrZero(obj.police)
    }
}