package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getIntOrZero


object DeliveringUtils : GSheetSerialized<DeliverToCustomerDataModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURLNew,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "deliverOrders",
    query = null,
    modelClass = DeliverToCustomerDataModel::class.java
) {

    fun get(name: String, useCache: Boolean = true): DeliverToCustomerDataModel? {
        val list = DeliveringUtils.fetchAll(useCache).execute()
        val filteredObj = list.filter { it.name == name }
        if (filteredObj.isEmpty())
            return null
        return filteredObj[0]
    }

    private fun calculateDeliverAmount(kg: Double, rate: Int): Int {
        val roundUpOffset = 0.000001
        return (kg * rate + roundUpOffset).toInt()
    }

    fun calculateDeliverAmount(kg: String, rate: String): Int {
        return calculateDeliverAmount(NumberUtils.getDoubleOrZero(kg), getIntOrZero(rate))
    }
}