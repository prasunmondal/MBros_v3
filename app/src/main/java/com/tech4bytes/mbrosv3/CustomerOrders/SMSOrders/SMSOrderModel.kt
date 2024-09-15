package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.io.Serializable
import java.util.Optional

data class SMSOrderModel(
    var id: String,
    var name: String,
    var orderedKg: Int,
    var appPc: String,
    var finalPc: Int,
    var orderedPc: Int,
    var avgWt1: String,
    var avgWt2: String,
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

    fun getAvgWt1(): String {
        return try {
            NumberUtils.getIntOrBlank(fetchAll().execute()[0].avgWt1)
        } catch (e: Exception) {
            ""
        }
    }

    fun getAvgWt2(): String {
        return try {
            NumberUtils.getIntOrBlank(fetchAll().execute()[0].avgWt2)
        } catch (e: Exception) {
            ""
        }
    }
}