package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import java.io.Serializable
import java.util.Optional

data class SMSOrderModel(
    var id: String,
    var name: String,
    var orderedKg: Int,
    var appPc: String,
    var finalPc: Int
): Serializable
object SMSOrderModelUtil: GSheetSerialized<SMSOrderModel> (
    context = ContextWrapper(AppContexts.get()),
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "GetOrders",
    modelClass = SMSOrderModel::class.java
) {
    fun getOrder(name: String): SMSOrderModel? {
        val list = fetchAll().execute()
        return try {
            list.filter { it.name == name }.stream().findFirst().get()
        } catch (e: Exception) {
            null
        }
    }
}