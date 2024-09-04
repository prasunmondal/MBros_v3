package com.tech4bytes.mbrosv3.Payments.Staged

import com.google.gson.reflect.TypeToken
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.Payments.PaymentsType
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

object StagedPaymentUtils : GSheetSerialized<StagedPaymentsModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURL,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "stagedPayments",
    query = null,
    modelClass = StagedPaymentsModel::class.java,) {

    fun getStagedPayments(name: String, useCache: Boolean = true): StagedPaymentsModel {

        // disable stagedpayents
        // delete the below return block to enable StagedPayments
        return StagedPaymentsModel(
            id = "",
            datetime = "",
            balanceBeforePayment = "",
            name = name,
            transactionType = PaymentsType.CREDIT,
            paidAmount = "",
            newBalance = "",
            paymentMode = "",
            notes = ""
        )


        val list = fetchAll().execute(useCache).filter { it.name == name }
        var sumPaid = 0
        var allNotes = ""
        list.forEach {
            sumPaid += NumberUtils.getIntOrZero(it.paidAmount)
            allNotes += "${it.name} paid Rs ${it.paidAmount} (${it.paymentMode}) recorded on ${it.datetime}."
            if (it.notes.trim().isEmpty()) {
                allNotes += "[Note: ${it.notes}]."
            }
        }
        return StagedPaymentsModel(
            id = "",
            datetime = "",
            balanceBeforePayment = "",
            name = name,
            transactionType = PaymentsType.CREDIT,
            paidAmount = sumPaid.toString(),
            newBalance = "",
            paymentMode = "",
            notes = allNotes
        )
    }
}