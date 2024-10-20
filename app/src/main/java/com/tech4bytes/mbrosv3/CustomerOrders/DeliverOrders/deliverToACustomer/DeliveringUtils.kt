package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig

object DeliveringUtils : GSheetSerialized<DeliverToCustomerDataModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURLNew,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "deliverOrders",
    query = null,
    modelClass = DeliverToCustomerDataModel::class.java
)