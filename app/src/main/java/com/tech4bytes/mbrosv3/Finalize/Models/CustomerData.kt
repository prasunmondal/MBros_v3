package com.tech4bytes.mbrosv3.Finalize.Models

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

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
        this.avgWt = (deliveredKg.toDouble() / deliveredPc.toInt()).toString()
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

        fun finalizeDelivers() {
            val deliveredData = DeliverCustomerOrders.get()
            deliveredData.forEach {
                val record = CustomerData(it.id, it.timestamp, it.name, it.deliveredPc, it.deliveredKg, it.rate, it.prevDue, it.todaysAmount, it.totalDue, it.paid, it.balanceDue, "0", "0")
                addToFinalizeSheet(record)
            }
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
        fun getRecords(): ArrayList<CustomerData> {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<ArrayList<CustomerData>>(AppContexts.get(), recordsKey, true)
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
    }
}