package com.tech4bytes.mbrosv3.BusinessData

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

data class SingleAttributedData(var timestamp: String = "",
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

        fun getRecords(): SingleAttributedData {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<SingleAttributedData>(AppContexts.get(), recordsKey, true)
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
            addRecordsToServer(obj)
            addRecordsToLocal(obj)
        }

        private fun getCombinedResultsFromList(list: List<SingleAttributedData>): SingleAttributedData {
            return list[0]
        }

        private fun getRecordsFromServer(): SingleAttributedData {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_FINALIZE_SHEET_ID)
                .tabName(SHEET_TABNAME)
                .build().execute()

            val recordsList = result.parseToObject<SingleAttributedData>(result.getRawResponse(), object: TypeToken<ArrayList<CustomerData>?>() {}.type)
            recordsList.sortBy { it.timestamp }
            recordsList.reverse()
            return getCombinedResultsFromList(recordsList)
        }

        private fun addRecordsToServer(record: SingleAttributedData) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_FINALIZE_SHEET_ID)
                .tabName(SHEET_TABNAME)
                .dataObject(record as Any)
                .build().execute()
        }

        private fun addRecordsToLocal(obj: SingleAttributedData) {
            CentralCache.put(recordsKey, obj)
        }
    }

}
