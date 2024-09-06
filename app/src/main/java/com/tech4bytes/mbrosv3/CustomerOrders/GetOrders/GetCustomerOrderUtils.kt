package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GScript
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.stream.Collectors
import kotlin.math.roundToInt

data class GetCustomerOrderModel(
    var id: String = "",
    var timestamp: String = "",
    var name: String = "",
    var seqNo: String = "",
    var orderedPc: String = "",
    var orderedKg: String = "",
    var calculatedPc: String = "",
    var calculatedKg: String = "",
    var rate: String = "",
    var prevDue: String = "",
) : java.io.Serializable {
    fun getEstimatedPc(allowFraction: Boolean): String {
        if (orderedPc.isNotEmpty()) {
            return orderedPc
        }
        val pc = NumberUtils.getDoubleOrZero(orderedKg) * 1000 / NumberUtils.getIntOrZero(SingleAttributedDataUtils.getRecords().estimatedLoadAvgWt)
        return if (allowFraction) {
            NumberUtils.roundOff2places(pc).toString()
        } else {
            pc.roundToInt().toString()
        }
    }

    fun getEstimatedKg(allowFraction: Boolean): String {
        if (orderedKg.isNotEmpty()) {
            return orderedKg
        }
        val kg = NumberUtils.getDoubleOrZero((NumberUtils.getIntOrZero(orderedPc) * (SingleAttributedDataUtils.getRecords().estimatedLoadAvgWt.toInt() / 1000)).toString(), "#.#")
        return if (allowFraction) {
            NumberUtils.roundOff2places(kg).toString()
        } else {
            kg.roundToInt().toString()
        }
    }
}

object GetCustomerOrderUtils : GSheetSerialized<GetCustomerOrderModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURLNew,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "GetOrders",
    query = null,
    modelClass = GetCustomerOrderModel::class.java,
    appendInServer = false,
    appendInLocal = false) {

    private var obj: MutableList<GetCustomerOrderModel> = mutableListOf()

    fun updateObj(passedObj: GetCustomerOrderModel) {
        var toBeRemoved: GetCustomerOrderModel? = null
        obj.forEach {
            if (it.name == passedObj.name) {
                toBeRemoved = it
            }
        }
        if (toBeRemoved != null) {
            obj.remove(toBeRemoved)
            obj.add(passedObj)
            LogMe.log("Updated: $passedObj")
        }

        val nameMappedOrders: MutableMap<String, GetCustomerOrderModel> = obj.stream()
            .collect(Collectors.toMap(GetCustomerOrderModel::name) { v -> v })
        LogMe.log(nameMappedOrders.toString())

        obj = mutableListOf()

        LogMe.log(CustomerKYC.fetchAll().execute().toString())
        CustomerKYC.fetchAll().execute().forEach {
            if (it.isActiveCustomer.toBoolean()) {
                obj.add(nameMappedOrders[it.nameEng]!!)
            }
        }
    }

    fun getListOfOrderedCustomers(): List<GetCustomerOrderModel> {
        val list: MutableList<GetCustomerOrderModel> = mutableListOf()
        val actualOrders = GetCustomerOrderUtils.fetchAll().execute()
        CustomerKYC.fetchAll().execute().forEach { masterList ->
            actualOrders.forEach { orderList ->
                if (masterList.nameEng == orderList.name) {
                    list.add(orderList)
                }
            }
        }
        return list
    }

    fun getListOfUnOrderedCustomers(): List<GetCustomerOrderModel> {
        val list: MutableList<GetCustomerOrderModel> = mutableListOf()
        val actualOrders = fetchAll().execute()
        CustomerKYC.fetchAll().execute().forEach { masterList ->
            var isInOrderList = false
            actualOrders.forEach { orderList ->
                if (masterList.nameEng == orderList.name) {
                    isInOrderList = true
                }
            }
            if (!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                list.add(GetCustomerOrderModel(name = masterList.nameEng))
            }
        }
        return list
    }

    fun getNumberOfCustomersOrdered(useCache: Boolean): Int {
        return fetchAll().execute(useCache).size
    }

    fun getByName(inputName: String): GetCustomerOrderModel? {
        fetchAll().execute().forEach {
            if (it.name == inputName) {
                return it
            }
        }
        return null
    }

    fun save() {
        obj.forEach {
            it.timestamp = DateUtils.getCurrentTimestamp()
        }

        getRecordsForOnlyOrderedCustomers().forEach {
            GetCustomerOrderUtils.insert(it).queue()
        }
        GetCustomerOrderUtils.fetchAll().queue()
        GScript.execute(false)
    }

    private fun getRecordsForOnlyOrderedCustomers(): MutableList<GetCustomerOrderModel> {
        return obj.stream().filter { p -> p.id.isNotEmpty() }.collect(Collectors.toList())
    }
}
