package com.tech4bytes.mbrosv3.BusinessData

import android.annotation.SuppressLint
import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1

data class SingleAttributedData(var recordGeneratorDevice: String = "",
                                var id: String = "",
                                var date: String = "",
                                var openingFarmRate: String = "",
                                var finalFarmRate: String = "",
                                var bufferRate: String = "",
                                var vehicle_prevKm: String = "",
                                var vehicle_finalKm: String = "",
                                var estimatedLoadPc: String = "",
                                var estimatedLoadKg: String = "",
                                var estimatedLoadAvgWt: String = "",
                                var actualLoadPc: String = "",
                                var actualLoadKg: String = "",
                                var actualLoadAvgWt: String = "",
                                var load_companyName: String = "",
                                var load_branch: String = "",
                                var load_account: String = "",
                                var refueling_qty: String = "",
                                var refueling_amount: String = "",
                                var refueling_isFullTank: String = "",
                                var refueling_prevKm: String = "",
                                var refueling_km: String = "") {

    companion object {

        private var recordsKey = "SingleAttributedMetadata"
        private var SHEET_TABNAME = "metadata"

        fun getRecords(useCache: Boolean = true): SingleAttributedData {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<SingleAttributedData>(AppContexts.get(), recordsKey, useCache)
            LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults!=null))
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getRecordsFromServer()

                CentralCache.put(recordsKey, resultFromServer)
                resultFromServer
            }
        }

        fun save(obj: SingleAttributedData) {
            obj.recordGeneratorDevice = getPhoneId()
            obj.id = System.currentTimeMillis().toString()
            obj.date = DateUtils.getCurrentTimestamp()
            addRecordsToServer(obj)
            addRecordsToLocal(obj)
        }

        fun saveAttribute(kMutableProperty: KMutableProperty1<SingleAttributedData, String>, value: String) {
            val obj = getRecords()
            ReflectionUtils.setAttribute(obj, kMutableProperty, value)
            save(obj)
        }

        fun getAttribute(kMutableProperty: KMutableProperty1<SingleAttributedData, String>): String {
            val obj = getRecords()
            return ReflectionUtils.readInstanceProperty(obj, kMutableProperty.name)
        }

        private fun getCombinedResultsFromList(list: List<SingleAttributedData>): SingleAttributedData {
            if(list.isEmpty()) return SingleAttributedData()
            return list[0]
        }

        private fun getRecordsFromServer(): SingleAttributedData {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(SHEET_TABNAME)
                .build().execute()

            val recordsList = result.parseToObject<SingleAttributedData>(result.getRawResponse(), object: TypeToken<ArrayList<SingleAttributedData>?>() {}.type)
            recordsList.sortBy { it.id }
            recordsList.reverse()
            return getCombinedResultsFromList(recordsList)
        }

        private fun addRecordsToServer(record: SingleAttributedData) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(SHEET_TABNAME)
                .dataObject(record as Any)
                .build().execute()
        }

        private fun addRecordsToLocal(obj: SingleAttributedData) {
            CentralCache.put(recordsKey, obj)
        }

        @SuppressLint("HardwareIds")
        private fun getPhoneId(): String {
            return Settings.Secure.getString(AppContexts.get().contentResolver,
                Settings.Secure.ANDROID_ID);
        }
    }

}