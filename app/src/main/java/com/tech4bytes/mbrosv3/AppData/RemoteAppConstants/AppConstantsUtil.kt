package com.tech4bytes.mbrosv3.AppData.RemoteAppConstants

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig

object AppConstantsUtil : GSheetSerialized<AppConstantModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURL,
    sheetURL = ProjectConfig.get_db_sheet_id(),
    tabName = "appConstants",
    classTypeForResponseParsing = AppConstantModel::class.java,
    appendInServer = true,
    appendInLocal = true
)