package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.mbrosv3.ProjectConfig

data class SMSOrderModel(var id: String, var name: String, var orderedKg: Int, var calculatedPc: Double, var orderedPc: Int) {

    companion object {
        val SHEET_SMS_ORDERS = "GetOrders"

        fun save(smsOrderEntry: SMSOrderModel) {
            saveToServer(smsOrderEntry)
        }

        private fun saveToServer(smsOrderEntry: SMSOrderModel) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(SHEET_SMS_ORDERS)
                .dataObject(smsOrderEntry as Any)
                .build().execute()
        }

        fun deleteAllDataInServer() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(SHEET_SMS_ORDERS)
                .build().execute()
        }
    }
}