package com.tech4bytes.mbrosv3.AppUsers

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.device.DeviceUtils
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientFilter
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig

object RolesUtils : GSheetSerialized<AppUsersModel>(
    ContextWrapper(AppContexts.get()),
    ProjectConfig.dBServerScriptURLNew,
    ProjectConfig.get_db_sheet_id(),
    "logins",
    query = null,
    modelClass = AppUsersModel::class.java,
    filter = ClientFilter("getUserForCurrentDevice") {list: List<AppUsersModel> -> list.filter { DeviceUtils.getUniqueID(AppContexts.get()) == it.device_id }}
) {
    fun getAppUser(useCache: Boolean = true): AppUsersModel? {
        val user = fetchAll(useCache).execute()
        if(user.isEmpty())
            return null
        return fetchAll(useCache).execute()[0]
    }
}
