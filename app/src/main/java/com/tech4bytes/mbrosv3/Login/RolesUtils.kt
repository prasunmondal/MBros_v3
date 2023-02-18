package com.tech4bytes.mbrosv3.Login

import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class RolesUtils {

    companion object {

        val loginRoleKey: String = "loginRoleKey"
        fun getRoles(useCache: Boolean = true): MutableList<Roles> {
            val cacheResults = CentralCache.get<MutableList<Roles>>(AppContexts.get(), loginRoleKey, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getRoleFromServer()

                CentralCache.put(loginRoleKey, resultFromServer)
                resultFromServer
            }
        }

        fun doesHaveRole(role: Roles): Boolean {
            return getRoles().contains(role)
        }

        private fun getRoleFromServer(): MutableList<Roles> {
            // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(Config.SHEET_TAB_NAME)
                .build().execute()

            val deviceList = result.parseToObject<RolesModel>(result.getRawResponse(), object: TypeToken<ArrayList<RolesModel>?>() {}.type)
            deviceList.sortBy { it.id }
            deviceList.reverse()

            deviceList.forEach {
                LogMe.log(it.toString())
            }

            val listOfRoles = mutableListOf<Roles>()
            deviceList.forEach {
                if(getPhoneId() == it.device_id) {
                    LogMe.log(it.roles)
                    LogMe.log(it.roles.split(",").toString())
                    it.roles.split(",").forEach { role ->
                        listOfRoles.add(Roles.valueOf(role.trim()))
                    }
                }
            }

            // waitDialog!!.dismiss()
            return listOfRoles
        }

        private fun getPhoneId(): String {
            return Settings.Secure.getString(AppContexts.get().contentResolver,
                Settings.Secure.ANDROID_ID);
        }
    }
}