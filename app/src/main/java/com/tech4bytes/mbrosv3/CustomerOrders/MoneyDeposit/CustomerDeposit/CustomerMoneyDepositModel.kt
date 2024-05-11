package com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit.CustomerDeposit

import java.io.Serializable

data class CustomerMoneyDepositModel(
    val id: String,
    val beneficiary: String,
    val mode: String,
    val debitAccount: String,
    val creditAccount: String,
    val debitAmount: String,
    val creditAmount: String,
    val handOverTo: String,
    val notes: String
) : Serializable {

    override fun toString(): String {
        return "MoneyDepositModel(id='$id', beneficiary='$beneficiary', mode='$mode', debitAccount='$debitAccount', creditAccount='$creditAccount', debitAmount='$debitAmount', creditAmount='$creditAmount', handOverTo='$handOverTo', notes='$notes')"
    }
}
