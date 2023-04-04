package com.tech4bytes.mbrosv3.Finalize.Models

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class CustomerData: java.io.Serializable {
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

    fun getCustomerNames(): HashSet<String> {
        val customerNames: HashSet<String> = hashSetOf()
        getRecords().forEach {
            customerNames.add(it.name)
        }
        return customerNames
    }

    companion object {

        fun spoolDeliveringData() {
            Thread {
                val deliveredData = DeliverCustomerOrders.get()
                val totalProfit = DaySummary.getDayProfit()
                LogMe.log("Total Profit: $totalProfit")
                var actualDeliveredKg = 0.0
                deliveredData.forEach {
                    actualDeliveredKg += NumberUtils.getDoubleOrZero(it.deliveredKg)
                }
                deliveredData.forEach {
                    val profitByCustomer = totalProfit * NumberUtils.getDoubleOrZero(it.deliveredKg) / actualDeliveredKg
                    LogMe.log("ProfitPerCustomer: Name: ${it.name}: $totalProfit * ${NumberUtils.getDoubleOrZero(it.deliveredKg)} / $actualDeliveredKg = $profitByCustomer")
                    val profitPercentByCustomer = profitByCustomer / totalProfit * 100
                    val record = CustomerData(it.id, it.timestamp, it.name, it.deliveredPc, it.deliveredKg, it.rate, it.prevDue, it.todaysAmount, it.totalDue, it.paid, it.balanceDue, NumberUtils.roundOff2places(profitByCustomer).toString(), NumberUtils.roundOff2places(profitPercentByCustomer).toString())
                    addToFinalizeSheet(record)
                }
            }.start()
        }

        private fun addToFinalizeSheet(record: CustomerData) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_FINALIZE_SHEET_ID)
                .tabName(FinalizeConfig.SHEET_FINALIZE_DELIVERIES_TAB_NAME)
                .dataObject(record as Any)
                .build().execute()
        }

        fun getAllLatestRecords(): MutableList<CustomerData> {
            val customerRecords = getRecords()
            customerRecords.sortBy { it.orderId }
            customerRecords.reverse()

            val addedNames = mutableListOf<String>()
            val latestRecordsList = mutableListOf<CustomerData>()
            customerRecords.forEach {
                if(!addedNames.contains(it.name)) {
                    latestRecordsList.add(it)
                    addedNames.add(it.name)
                }
            }
            return latestRecordsList
        }

        fun getLastDue(name: String): String {
            val customerRecords = getAllLatestRecords()
            customerRecords.forEach {
                if(it.name == name)
                    return it.balanceDue
            }
            return "0"
        }

        private var recordsKey = "customerRecords"
        fun getRecords(useCache: Boolean = true): ArrayList<CustomerData> {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<ArrayList<CustomerData>>(AppContexts.get(), recordsKey, useCache)
            LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults!=null))
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getRecordsFromServer()

                CentralCache.put(recordsKey, resultFromServer)
                resultFromServer
            }
        }

        private fun getRecordsFromServer(): ArrayList<CustomerData> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_FINALIZE_SHEET_ID)
                .tabName(FinalizeConfig.SHEET_FINALIZE_DELIVERIES_TAB_NAME)
                .build().execute()

            val recordsList = result.parseToObject<CustomerData>(result.getRawResponse(), object: TypeToken<ArrayList<CustomerData>?>() {}.type)
            recordsList.sortBy { it.orderId }
            recordsList.reverse()
            return recordsList
        }

        fun getCustomerDefaultRate(name: String): Int {
            return SingleAttributedData.getFinalRateInt() + SingleAttributedData.getBufferRateInt() + CustomerKYC.get(name)!!.rateDifference.toInt()
        }
    }
}