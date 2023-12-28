package com.tech4bytes.mbrosv3.AppUsers

import android.provider.Settings
import android.provider.Settings.Secure.ANDROID_ID
import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

object RolesUtils : Tech4BytesSerializable<AppUsersModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "logins",
    object : TypeToken<ArrayList<AppUsersModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true,
    getEmptyListIfNoResultsFoundInServer = true
) {

    fun getAppUser(useCache: Boolean = true, getEmptyListIfEmpty: Boolean = false, filterName: String = "default"): AppUsersModel? {

        val list = super.get(useCache, getEmptyListIfEmpty, filterName)
        if(list.isEmpty())
            return null

        return list[0]
    }

    override fun <T : Any> filterResults(list: ArrayList<T>): ArrayList<T> {
        if(list.isEmpty())
            return arrayListOf()

        var list2 = list as List<AppUsersModel?>
        list2.sortedBy { it!!.id }
        list2 = list2.reversed()

        list2.forEach {
            LogMe.log(it.toString())
            if (getPhoneId() == it!!.device_id) {
                return arrayListOf(it as T)
            }
        }
        return arrayListOf()
    }

    private fun getPhoneId(): String {
        return Settings.Secure.getString(
            AppContexts.get().contentResolver,
            ANDROID_ID
        )
    }
}
