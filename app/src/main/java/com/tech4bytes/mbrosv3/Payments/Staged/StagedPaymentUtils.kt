package com.tech4bytes.mbrosv3.Payments.Staged

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.Payments.PaymentsType
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

object StagedPaymentUtils : Tech4BytesSerializable<StagedPaymentsModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "stagedPayments",
    query = null,
    object : TypeToken<ArrayList<StagedPaymentsModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true) {

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


        val list = get(useCache).filter { it.name == name }
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