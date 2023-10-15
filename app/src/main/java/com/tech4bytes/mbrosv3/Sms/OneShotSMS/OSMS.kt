package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class OSMS {

    companion object {

        fun get(useCache: Boolean = true): List<OSMSModel> {
            val cacheKey = OSMSConfig.SHEET_TEMPLATE_TAB_NAME
            val cacheResults = CentralCache.get<ArrayList<OSMSModel>>(AppContexts.get(), cacheKey, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(cacheKey, resultFromServer)
                resultFromServer
            }
        }


        private fun getFromServer(): List<OSMSModel> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(OSMSConfig.SHEET_TEMPLATE_TAB_NAME)
                .build().execute()

            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<OSMSModel>?>() {}.type
            )
        }
    }
}