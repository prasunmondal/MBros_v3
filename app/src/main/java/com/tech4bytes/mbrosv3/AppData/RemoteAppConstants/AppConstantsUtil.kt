package com.tech4bytes.mbrosv3.AppData.RemoteAppConstants

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig

object AppConstantsUtil : Tech4BytesSerializable<AppConstantModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "appConstants",
    query = null,
    object : TypeToken<ArrayList<AppConstantModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true,
    getEmptyListIfNoResultsFoundInServer = true)