package com.tech4bytes.mbrosv3.AppData.RemoteAppConstants

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class AppConstantsUtil {

    companion object {
        val SHEET_TAB_NAME = "appConstants";

        fun getAll(useCache: Boolean = true): List<AppConstantModel> {
            LogMe.log("Getting App Constants: tryCache: $useCache")
            var cacheResults = CentralCache.get<List<AppConstantModel>>(AppContexts.get(), SHEET_TAB_NAME, useCache)

            if (cacheResults == null) {
                LogMe.log("Getting delivery data: Cache failed")
                cacheResults = getFromServer()
                saveToLocal(cacheResults)
            }

            return cacheResults
        }

        private fun saveToLocal(list: List<AppConstantModel>) {
            CentralCache.put(SHEET_TAB_NAME, list)
        }

        private fun getFromServer(): List<AppConstantModel> {
            LogMe.log("Getting App Constants: Getting from server")
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(SHEET_TAB_NAME)
                .build().execute()

            return result.parseToObject(result.getRawResponse())
        }
    }
}