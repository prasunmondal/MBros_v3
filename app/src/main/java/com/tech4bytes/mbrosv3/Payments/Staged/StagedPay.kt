package com.tech4bytes.mbrosv3.Payments.Staged

import com.tech4bytes.mbrosv3.Payments.PaymentsType
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.*

class StagedPay {

    companion object {

        fun transact(
            name: String,
            prevBalance: String,
            txnType: String,
            amountPaid: String,
            txnMode: String,
            notes: String,
        ) {
            val stagedObj = StagedPaymentsModel(
                id = System.currentTimeMillis().toString(),
                datetime = DateUtils.getCurrentTimestamp(),
                name = name,
                prevBalance = prevBalance,
                transactionType = PaymentsType.valueOf(txnType.uppercase(Locale.ROOT)),
                paidAmount = amountPaid,
                paymentMode = txnMode,
                notes = notes,
                newBalance = "0"
            )

            var paidAmountInt = NumberUtils.getIntOrZero(stagedObj.paidAmount)
            if (stagedObj.transactionType == PaymentsType.DEBIT) {
                paidAmountInt *= -1
            }
            stagedObj.newBalance = (NumberUtils.getIntOrZero(prevBalance) - paidAmountInt).toString()
            StagedPaymentUtils.saveToServerThenLocal(stagedObj)
        }

        fun transact(transObj: StagedPaymentsModel) {
            StagedPaymentUtils.saveToServerThenLocal(transObj)
        }
    }
}
