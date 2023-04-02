package com.tech4bytes.mbrosv3.AppUsers

import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class RolesUtils {

    companion object {

        val loginRoleKey: String = "loginRoleKey"
        fun getAppUser(useCache: Boolean = true): AppUsersModel? {
            val cacheResults = CentralCache.get<AppUsersModel>(AppContexts.get(), loginRoleKey, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getAppUsersDataFromServer()

                CentralCache.put(loginRoleKey, resultFromServer)
                resultFromServer
            }
        }

        private fun getAppUsersDataFromServer(): AppUsersModel? {
            // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(Config.SHEET_TAB_NAME)
                .build().execute()

            val deviceList = result.parseToObject<AppUsersModel>(result.getRawResponse(), object: TypeToken<ArrayList<AppUsersModel>?>() {}.type)
            deviceList.sortBy { it.id }
            deviceList.reverse()

            deviceList.forEach {
                LogMe.log(it.toString())
            }


            deviceList.forEach {
                if(getPhoneId() == it.device_id) {
                    return it
                }
            }
            return null
        }

        private fun getPhoneId(): String {
            return Settings.Secure.getString(AppContexts.get().contentResolver,
                Settings.Secure.ANDROID_ID);
        }
    }
}