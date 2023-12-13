package com.tech4bytes.mbrosv3.Payments.Staged

import com.tech4bytes.mbrosv3.Payments.PaymentsType
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

data class StagedPaymentsModel(
    var id: String = System.currentTimeMillis().toString(),
    var datetime: String = DateUtils.getCurrentTimestamp(),
    var name: String,
    var transactionType: PaymentsType,
    var prevBalance: String,
    var paidAmount: String,
    var newBalance: String,
    var paymentMode: String,
    var notes: String
): java.io.Serializable {

    companion object {
        var sheet = ProjectConfig.get_db_sheet_id()
        var tabname = "stagedPayments"
    }
}
