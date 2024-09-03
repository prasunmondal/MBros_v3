package com.tech4bytes.mbrosv3.AppUsers

import com.prasunmondal.dev.libs.device.DeviceUtils
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientFilter
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import com.prasunmondal.dev.libs.contexts.AppContexts

object RolesUtils : GSheetSerialized<AppUsersModel>(
    ContextWrapper(AppContexts.get()),
    ProjectConfig.dBServerScriptURLNew,
    ProjectConfig.get_db_sheet_id(),
    "logins",
    query = null,
    classTypeForResponseParsing = AppUsersModel::class.java,
    appendInServer = true,
    appendInLocal = true,
    filter = ClientFilter("getUserForCurrentDevice") {list: List<AppUsersModel> -> list.filter { DeviceUtils.getUniqueID(AppContexts.get()) == it.device_id }}
) {
    fun getAppUser(useCache: Boolean = true): AppUsersModel? {
        val user = fetchAll().execute(useCache)
        if(user.isEmpty())
            return null
        return fetchAll().execute(useCache)[0]
    }
}
