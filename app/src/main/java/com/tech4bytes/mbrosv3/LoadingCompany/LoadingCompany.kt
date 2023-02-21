package com.tech4bytes.mbrosv3.LoadingCompany

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

data class LoadingCompany(val timestamp: Long,
                          val date: String,
                          val companyName: String,
                          val branch: String,
                          val account: String,
                          val openingRate: String,
                          val closingRate: String) {

    companion object {

        fun get(): LoadingCompany? {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<LoadingCompany>(AppContexts.get(), LoadingCompanyConfig.SHEET_LOADING_COMPANY_DATA, true)
            LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults!=null))
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()

                CentralCache.put(LoadingCompanyConfig.SHEET_LOADING_COMPANY_DATA, resultFromServer)
                resultFromServer
            }
        }

        fun save(obj: LoadingCompany) {
            saveToServer(obj)
            saveToLocal(obj)
        }

        private fun saveToServer(obj: LoadingCompany) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(LoadingCompanyConfig.SHEET_LOADING_COMPANY_DATA)
                .dataObject(obj as Any)
                .build().execute()
        }

        private fun saveToLocal(obj: LoadingCompany) {
            CentralCache.put(LoadingCompanyConfig.SHEET_LOADING_COMPANY_DATA, obj)
        }

        private fun getFromServer(): LoadingCompany? {
            // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(LoadingCompanyConfig.SHEET_LOADING_COMPANY_DATA)
                .build().execute()

            val recordsList = result.parseToObject<LoadingCompany>(result.getRawResponse(), object: TypeToken<ArrayList<LoadingCompany>?>() {}.type)
            recordsList.sortBy { it.timestamp }
            recordsList.reverse()
            return if(recordsList.isEmpty())
                null
            else
                recordsList[0]
        }
    }
}
