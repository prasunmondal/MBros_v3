package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig

object OSMS: GSheetSerialized<OSMSModel>(
    context = ContextWrapper(AppContexts.get()),
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "smsModel",
    modelClass = OSMSModel::class.java
)