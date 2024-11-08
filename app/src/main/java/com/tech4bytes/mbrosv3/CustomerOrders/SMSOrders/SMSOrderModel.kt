package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.io.Serializable

data class SMSOrderModel(
    var id: String,
    var name: String,
    var orderedKg: Int,
    var appPc: String,
    var finalPc: Int,
    var orderedPc: Int,
    var avgWt1: String,
    var avgWt2: String,
    var extraPc: String,
    var extraKg: String
): Serializable
object SMSOrderModelUtil: GSheetSerialized<SMSOrderModel> (
    context = ContextWrapper(AppContexts.get()),
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "GetOrders",
    modelClass = SMSOrderModel::class.java
) {

    private fun createOrderObj(name: String): SMSOrderModel {
        return SMSOrderModel(id = System.currentTimeMillis().toString(),
            name = name,
            orderedKg = 0,
            appPc = "",
            finalPc = 0,
            orderedPc = 0,
            avgWt1 = "0",
            avgWt2 = "0",
            extraPc = "",
            extraKg = "")
    }

    fun getOrder(list: List<SMSOrderModel>, name: String): SMSOrderModel? {
        return try {
            list.filter { it.name == name }.stream().findFirst().get()
        } catch (e: Exception) {
            null
        }
    }

    fun getOrder(name: String): SMSOrderModel? {
        val list = fetchAll().execute()
        return getOrder(list, name)
    }

    fun getListOfOrderedCustomers(): List<SMSOrderModel> {
        val list: MutableList<SMSOrderModel> = mutableListOf()
        val actualOrders = SMSOrderModelUtil.fetchAll().execute()
        CustomerKYC.fetchAll().execute().forEach { masterList ->
            actualOrders.forEach { orderList ->
                if (masterList.nameEng == orderList.name) {
                    list.add(orderList)
                }
            }
        }
        return list
    }

    fun getListOfUnOrderedCustomers(): List<SMSOrderModel> {
        val list: MutableList<SMSOrderModel> = mutableListOf()
        val actualOrders = SMSOrderModelUtil.fetchAll().execute()
        CustomerKYC.fetchAll().execute().forEach { masterList ->
            var isInOrderList = false
            actualOrders.forEach { orderList ->
                if (masterList.nameEng == orderList.name) {
                    isInOrderList = true
                }
            }
            if (!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                list.add(createOrderObj(masterList.nameEng))
            }
        }
        return list
    }

    fun getAvgWt1(): String {
        return try {
            fetchAll().execute()[0].avgWt1.toInt().toString()
        } catch (e: Exception) {
            NumberUtils.getDoubleOrBlank(fetchAll().execute()[0].avgWt1)
        }
    }

    fun getAvgWt2(): String {
        return try {
            fetchAll().execute()[0].avgWt2.toInt().toString()
        } catch (e: Exception) {
            NumberUtils.getDoubleOrBlank(fetchAll().execute()[0].avgWt2)
        }
    }

    fun getTotalOrderedPc(): Int {
        val actualOrders = SMSOrderModelUtil.fetchAll().execute()
        var sumPc = 0
        actualOrders.forEach {
            sumPc += it.orderedPc
        }
        if(actualOrders.isNotEmpty())
            sumPc += NumberUtils.getIntOrZero(actualOrders[0].extraPc)
        return sumPc
    }
}