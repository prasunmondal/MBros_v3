package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.stream.Collectors
import kotlin.math.roundToInt


data class GetCustomerOrders(
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
        val pc = NumberUtils.getDoubleOrZero(orderedKg) * 1000 / NumberUtils.getIntOrZero(SingleAttributedData.getRecords().estimatedLoadAvgWt)
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
        val kg = NumberUtils.getDoubleOrZero((NumberUtils.getIntOrZero(orderedPc) * (SingleAttributedData.getRecords().estimatedLoadAvgWt.toInt() / 1000)).toString(), "#.#")
        return if (allowFraction) {
            NumberUtils.roundOff2places(kg).toString()
        } else {
            kg.roundToInt().toString()
        }
    }

    companion object {

        val SHEET_INDIVIDUAL_ORDERS_TAB_NAME = "GetOrders"
        var obj: MutableList<GetCustomerOrders> = mutableListOf()

        fun get(useCache: Boolean = true): List<GetCustomerOrders> {
            val cacheResults = CentralCache.get<ArrayList<GetCustomerOrders>>(AppContexts.get(), SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            obj = if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = GetCustomerOrdersUtils.getCompleteList()
                CentralCache.put(SHEET_INDIVIDUAL_ORDERS_TAB_NAME, resultFromServer)
                resultFromServer as MutableList<GetCustomerOrders>
            }
            return obj
        }

        fun getServerList(useCache: Boolean = true): List<GetCustomerOrders> {
            val getOrdersServerListKey = "getOrdersServerList"
            val cacheResults = CentralCache.get<ArrayList<GetCustomerOrders>>(AppContexts.get(), getOrdersServerListKey, useCache)

            obj = if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(getOrdersServerListKey, resultFromServer)
                resultFromServer as MutableList<GetCustomerOrders>
            }
            return obj
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
                .tabName(SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
            deleteFromLocal()
        }

        private fun saveToServer() {
            GetCustomerOrdersUtils.getRecordsForOnlyOrderedCustomers().forEach {
                if (NumberUtils.getIntOrZero(it.orderedKg) > 0 || NumberUtils.getIntOrZero(it.orderedPc) > 0) {
                    PostObject.builder()
                        .scriptId(ProjectConfig.dBServerScriptURL)
                        .sheetId(ProjectConfig.get_db_sheet_id())
                        .tabName(SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                        .dataObject(it as Any)
                        .build().execute()
                }
            }
        }

        fun saveToLocal() {
            CentralCache.put(SHEET_INDIVIDUAL_ORDERS_TAB_NAME, obj)
        }

        private fun deleteFromLocal() {
            CentralCache.put(SHEET_INDIVIDUAL_ORDERS_TAB_NAME, listOf<GetCustomerOrders>())
        }

        private fun getFromServer(): List<GetCustomerOrders> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()

            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<GetCustomerOrders>?>() {}.type
            )
        }
    }
}