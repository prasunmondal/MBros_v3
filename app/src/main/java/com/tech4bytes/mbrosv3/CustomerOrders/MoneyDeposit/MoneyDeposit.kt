package com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import java.util.Calendar
import java.util.Date

object MoneyDeposit: Tech4BytesSerializable<MoneyDepositModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "moneyDeposits",
    object : TypeToken<ArrayList<MoneyDepositModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true
) {

    override fun <T : Any> sortResults(list: ArrayList<T>): ArrayList<T> {
        if(list.isEmpty())
            return arrayListOf()

        (list as MutableList<MoneyDepositModel>).sortBy { it.id }
        list.reverse()
        val t: ArrayList<MoneyDepositModel> = arrayListOf()
        t.addAll(list)
        return t as ArrayList<T>
    }
}