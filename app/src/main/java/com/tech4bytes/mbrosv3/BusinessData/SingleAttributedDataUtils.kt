package com.tech4bytes.mbrosv3.BusinessData

import android.annotation.SuppressLint
import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1

object SingleAttributedDataUtils : Tech4BytesSerializable<SingleAttributedDataModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "metadata",
    object : TypeToken<ArrayList<SingleAttributedDataModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true,
    getEmptyListIfNoResultsFoundInServer = true) {

    override fun <T : Any> sortResults(list: ArrayList<T>): ArrayList<T> {
        ListUtils.sortListByAttribute(list as ArrayList<SingleAttributedDataModel>, SingleAttributedDataModel::id)
        list.sortBy { it.id }
        list.reverse()
        val t: ArrayList<SingleAttributedDataModel> = arrayListOf()
        t.add(list[0])
        return t as ArrayList<T>
    }

    fun getRecords(useCache: Boolean = true): SingleAttributedDataModel {
        return get(useCache)[0]
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
            obj.recordGeneratorDevice = getPhoneId()
            obj.date = DateUtils.getCurrentTimestamp()
        }
        super.saveToLocal(obj, null)
    }

    @SuppressLint("HardwareIds")
    private fun getPhoneId(): String {
        return Settings.Secure.getString(
            AppContexts.get().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun invalidateCache() {
        saveToLocal(null)
    }

    fun getExtraExpenseExcludingPolice(obj: SingleAttributedDataModel): Int {
        return NumberUtils.getIntOrZero(obj.extra_expenses) - NumberUtils.getIntOrZero(obj.police)
    }
}