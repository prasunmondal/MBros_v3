package com.tech4bytes.mbrosv3.BusinessData

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class SheetCalculator: java.io.Serializable {

    var is_khata_green: String = ""

    companion object {

        private var recordsKey = "syncSheetCalculator"
        private var SHEET_TABNAME = "syncSheetCalculator"

        fun getRecords(useCache: Boolean = true): SheetCalculator {
            LogMe.log("Getting SheetCalculator Records")
            val cacheResults = CentralCache.get<SheetCalculator>(AppContexts.get(), recordsKey, useCache)

            LogMe.log("Getting SheetCalculator records: Cache Hit: " + (cacheResults != null))
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getRecordsFromServer()
                CentralCache.put(recordsKey, resultFromServer)
                resultFromServer
            }
        }

        fun isKhataGreen(useCache: Boolean): Boolean {
            return getRecords(useCache).is_khata_green.toBoolean()
        }

        private fun getRecordsFromServer(): SheetCalculator {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(SHEET_TABNAME)
                .build().execute()

            val recordsList = result.parseToObject<SheetCalculator>(result.getRawResponse(), object : TypeToken<ArrayList<SheetCalculator>?>() {}.type)
            return recordsList[0]
        }
    }
}