package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
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

object GetCustomerOrderUtils : Tech4BytesSerializable<GetCustomerOrderModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "GetOrders",
    object : TypeToken<ArrayList<GetCustomerOrderModel>?>() {}.type,
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

        LogMe.log(CustomerKYC.get().toString())
        CustomerKYC.get().forEach {
            if (it.isActiveCustomer.toBoolean()) {
                obj.add(nameMappedOrders[it.nameEng]!!)
            }
        }
        saveToLocal()
    }

    private fun getCompleteList(): List<GetCustomerOrderModel> {
        val list: MutableList<GetCustomerOrderModel> = mutableListOf()
        val actualOrders = getServerList()
        CustomerKYC.get().forEach { masterList ->
            var isInOrderList = false
            actualOrders.forEach { orderList ->
                if (masterList.nameEng == orderList.name) {
                    list.add(orderList)
                    isInOrderList = true
                }
            }
            if (!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                list.add(GetCustomerOrderModel(name = masterList.nameEng))
            }
        }
        return list
    }

    fun getListOfOrderedCustomers(): List<GetCustomerOrderModel> {
        val list: MutableList<GetCustomerOrderModel> = mutableListOf()
        val actualOrders = getServerList()
        CustomerKYC.get().forEach { masterList ->
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
        val actualOrders = getServerList()
        CustomerKYC.get().forEach { masterList ->
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
        return get(useCache).size
    }

    fun getByName(inputName: String): GetCustomerOrderModel? {
        get().forEach {
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
        saveToServer()
        saveToLocal()
    }

    fun deleteAll() {
        Delete.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.get_db_sheet_id())
            .tabName(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
            .build().execute()
        deleteFromLocal()
    }

    private fun saveToServer() {
        getRecordsForOnlyOrderedCustomers().forEach {
            if (NumberUtils.getIntOrZero(it.orderedKg) > 0 || NumberUtils.getIntOrZero(it.orderedPc) > 0) {
                PostObject.builder()
                    .scriptId(ProjectConfig.dBServerScriptURL)
                    .sheetId(ProjectConfig.get_db_sheet_id())
                    .tabName(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                    .dataObject(it as Any)
                    .build().execute()
            }
        }
    }

    private fun getRecordsForOnlyOrderedCustomers(): MutableList<GetCustomerOrderModel> {
        return obj.stream().filter { p -> p.id.isNotEmpty() }.collect(Collectors.toList())
    }

    fun saveToLocal() {
        saveToLocal(obj)
    }

    fun saveToLocal(obj: MutableList<GetCustomerOrderModel>) {
        CentralCache.put(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, obj)
    }

    fun deleteFromLocal() {
        CentralCache.put(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, listOf<GetCustomerOrderModel>())
    }

    private fun getFromServer(): List<GetCustomerOrderModel> {
        // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
        val result: GetResponse = Get.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.get_db_sheet_id())
            .tabName(CustomerOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
            .build().execute()

        // waitDialog!!.dismiss()
        return result.parseToObject(
            result.getRawResponse(),
            object : TypeToken<ArrayList<GetCustomerOrderModel>?>() {}.type
        )
    }

    private fun getServerList(useCache: Boolean = true): List<GetCustomerOrderModel> {
        val getOrdersServerListKey = "getOrdersServerList"
        val cacheResults = CentralCache.get<ArrayList<GetCustomerOrderModel>>(AppContexts.get(), getOrdersServerListKey, useCache)

        obj = if (cacheResults != null) {
            cacheResults
        } else {
            val resultFromServer = getFromServer()
            CentralCache.put(getOrdersServerListKey, resultFromServer)
            resultFromServer as MutableList<GetCustomerOrderModel>
        }
        return obj
    }
}
