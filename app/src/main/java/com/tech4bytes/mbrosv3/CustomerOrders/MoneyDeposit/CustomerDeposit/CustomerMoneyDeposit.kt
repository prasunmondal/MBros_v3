package com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit.CustomerDeposit

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig

object CustomerMoneyDeposit: Tech4BytesSerializable<CustomerMoneyDepositModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "moneyDeposits",
    query = null,
    object : TypeToken<ArrayList<CustomerMoneyDepositModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true
) {
    override fun <T : Any> sortResults(list: ArrayList<T>): ArrayList<T> {
        if(list.isEmpty())
            return arrayListOf()

        (list as MutableList<CustomerMoneyDepositModel>).sortBy { it.id }
        list.reverse()
        val t: ArrayList<CustomerMoneyDepositModel> = arrayListOf()
        t.addAll(list)
        return t as ArrayList<T>
    }
}