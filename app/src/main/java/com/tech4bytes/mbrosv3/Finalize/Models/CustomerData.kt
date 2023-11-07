package com.tech4bytes.mbrosv3.Finalize.Models

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import kotlin.collections.ArrayList

class CustomerData : java.io.Serializable {
    var orderId = ""
    var timestamp = ""
    var name = ""
    var deliveredPc = ""
    var deliveredKg = ""
    var rate = ""
    var prevAmount = ""
    var deliveredAmount = ""
    var totalAmount = ""
    var paid = ""
    var balanceDue = ""
    var avgWt = ""
    var profit = ""
    var profitPercent = ""

    constructor(orderId: String, timestamp: String, name: String, deliveredPc: String, deliveredKg: String, rate: String, prevAmount: String, deliveredAmount: String, totalAmount: String, paid: String, balanceDue: String, profit: String, profitPercent: String) {
        this.orderId = orderId
        this.timestamp = timestamp
        this.name = name
        this.deliveredPc = deliveredPc
        this.deliveredKg = deliveredKg
        this.rate = rate
        this.prevAmount = prevAmount
        this.deliveredAmount = deliveredAmount
        this.totalAmount = totalAmount
        this.paid = paid
        this.balanceDue = balanceDue
        this.avgWt = NumberUtils.roundOff3places(deliveredKg.toDouble() / deliveredPc.toInt()).toString()
        this.profit = profit
        this.profitPercent = profitPercent
    }

    override fun toString(): String {
        return "CustomerData(orderId='$orderId', timestamp='$timestamp', name='$name', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', prevAmount='$prevAmount', deliveredAmount='$deliveredAmount', totalAmount='$totalAmount', paid='$paid', balanceDue='$balanceDue', avgWt='$avgWt', profit='$profit', profitPercent='$profitPercent')"
    }

    companion object {
        const val TAB_NAME = "deliveries"
        const val cacheKey = TAB_NAME

        internal fun addToFinalizeSheet(record: CustomerData) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_finalize_sheet_id())
                .tabName(TAB_NAME)
                .dataObject(record as Any)
                .build().execute()
        }

        fun getRecords(useCache: Boolean = true): List<CustomerData> {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<ArrayList<CustomerData>>(AppContexts.get(), cacheKey, useCache)
            LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getRecordsFromServer()
                parseAndSaveToLocal(cacheKey, resultFromServer)
            }
        }

        private fun getRecordsFromServer(): GetResponse {
            return Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_finalize_sheet_id())
                .tabName(TAB_NAME)
                .build().execute()
        }

        fun parseToObject(result: GetResponse): ArrayList<CustomerData> {
            val recordsList = result.parseToObject<CustomerData>(result.getRawResponse(), object : TypeToken<ArrayList<CustomerData>?>() {}.type)
            recordsList.sortBy { it.orderId }
            recordsList.reverse()
            return recordsList
        }

        fun parseAndSaveToLocal(cacheKey: String, result: GetResponse): List<CustomerData> {
            val parsedObj = parseToObject(result)
            CentralCache.put(cacheKey, parsedObj)
            return parsedObj
        }
    }
}