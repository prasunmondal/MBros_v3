package com.tech4bytes.mbrosv3.BusinessData

import android.annotation.SuppressLint
import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1

object SingleAttributedDataUtils: Tech4BytesSerializable (
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "metadata",
    appendInServer = true,
    appendInLocal = true,
    cacheObjectType = object : TypeToken<ArrayList<SingleAttributedDataModel?>?>() {}.type,
    getEmptyListIfNoResultsFoundInServer = true) {

        private var recordsKey = "SingleAttributedMetadata"
        private var SHEET_TABNAME = "metadata"

        fun getRecords(useCache: Boolean = true): SingleAttributedDataModel {
            return get<SingleAttributedDataModel>(useCache)[0]
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

//        fun save4(obj: SingleAttributedDataModel) {
//            saveToLocal(obj)
//            saveToServer(obj)
//        }

        fun saveAttribute(kMutableProperty: KMutableProperty1<SingleAttributedDataModel, String>, value: String) {
            val obj = getRecords()
            ReflectionUtils.setAttribute(obj, kMutableProperty, value)
            saveToLocalThenServer(obj)
        }

        fun saveAttributeToLocal(kMutableProperty: KMutableProperty1<SingleAttributedDataModel, String>, value: String) {
            val obj = getRecords()
            ReflectionUtils.setAttribute(obj, kMutableProperty, value)
            saveToLocal(obj)
        }

        fun getAttribute(kMutableProperty: KMutableProperty1<SingleAttributedDataUtils, String>): String {
            val obj = getRecords()
            return ReflectionUtils.readInstanceProperty(obj, kMutableProperty.name)
        }

        override fun <T : Any> sortResults(list: ArrayList<T>): ArrayList<T> {
            ListUtils.sortListByAttribute(list as ArrayList<SingleAttributedDataModel>, SingleAttributedDataModel::id)
            list.sortBy { it.id }
            list.reverse()
            val t: ArrayList<SingleAttributedDataModel> = arrayListOf()
            t.add(list[0])
            return t as ArrayList<T>
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
    }

