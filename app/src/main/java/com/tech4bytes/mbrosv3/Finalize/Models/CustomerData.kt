package com.tech4bytes.mbrosv3.Finalize.Models

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.*
import java.util.stream.Collectors

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
    var paidCash = ""
    var paidOnline = ""
    var paid = ""
    var customerAccount = ""
    var balanceDue = ""
    var avgWt = ""
    var profit = ""
    var profitPercent = ""

    constructor(orderId: String, timestamp: String, name: String, deliveredPc: String, deliveredKg: String, rate: String, prevAmount: String, deliveredAmount: String, totalAmount: String, paidCash: String, paidOnline: String, paid: String, customerAccount: String, balanceDue: String, profit: String, profitPercent: String) {
        this.orderId = orderId
        this.timestamp = timestamp
        this.name = name
        this.deliveredPc = deliveredPc
        this.deliveredKg = deliveredKg
        this.rate = rate
        this.prevAmount = prevAmount
        this.deliveredAmount = deliveredAmount
        this.totalAmount = totalAmount
        this.paidCash = paidCash
        this.paidOnline = paidOnline
        this.paid = paid
        this.customerAccount = customerAccount
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

    override fun toString(): String {
        return "CustomerData(orderId='$orderId', timestamp='$timestamp', name='$name', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', prevAmount='$prevAmount', deliveredAmount='$deliveredAmount', totalAmount='$totalAmount', paid='$paid', balanceDue='$balanceDue', avgWt='$avgWt', profit='$profit', profitPercent='$profitPercent')"
    }

    companion object {

        fun spoolDeliveringData() {
            var deliveredData = DeliverToCustomerDataHandler.get()
            deliveredData = Sorter.sortByNameList(deliveredData, DeliverToCustomerDataModel::name) as List<DeliverToCustomerDataModel>

            val totalProfit = DaySummary.getDayProfit()
            LogMe.log("Total Profit: $totalProfit")
            var actualDeliveredKg = 0.0
            deliveredData.forEach {
                actualDeliveredKg += NumberUtils.getDoubleOrZero(it.deliveredKg)
            }
            deliveredData.forEach {
                val profitByCustomer = totalProfit * NumberUtils.getDoubleOrZero(it.deliveredKg) / actualDeliveredKg
                val profitPercentByCustomer = profitByCustomer / totalProfit * 100
                LogMe.log("ProfitPerCustomer: Name: ${it.name}: $totalProfit * ${NumberUtils.getDoubleOrZero(it.deliveredKg)} / $actualDeliveredKg = $profitByCustomer")

                it.timestamp = DateUtils.getDateInFormat(Date(it.id.toLong()), "M/d/yyyy")
                val record = CustomerData(it.id, it.timestamp, it.name, it.deliveredPc, it.deliveredKg, it.rate, it.prevDue, it.todaysAmount, it.totalDue, it.paidCash, it.paidOnline, it.paid, it.customerAccount, it.balanceDue, NumberUtils.roundOff2places(profitByCustomer).toString(), NumberUtils.roundOff2places(profitPercentByCustomer).toString())
                addToFinalizeSheet(record)
            }
        }

        private fun addToFinalizeSheet(record: CustomerData) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_finalize_sheet_id())
                .tabName(FinalizeConfig.SHEET_FINALIZE_DELIVERIES_TAB_NAME)
                .dataObject(record as Any)
                .build().execute()
        }

        fun getAllLatestRecords(useCache: Boolean = true): MutableList<CustomerData> {
            val customerRecords = getRecords(useCache)
            customerRecords.sortBy { it.orderId }
            customerRecords.reverse()

            val addedNames = mutableListOf<String>()
            val latestRecordsList = mutableListOf<CustomerData>()
            customerRecords.forEach {
                if (!addedNames.contains(it.name)) {
                    latestRecordsList.add(it)
                    addedNames.add(it.name)
                }
            }
            return latestRecordsList
        }

        fun getAllLatestRecordsByAccount(useCache: Boolean = true): MutableList<CustomerData> {
            val customerRecords = getRecords(useCache)
            customerRecords.sortBy { it.orderId }
            customerRecords.reverse()

            val addedNames = mutableListOf<String>()
            val latestRecordsList = mutableListOf<CustomerData>()
            customerRecords.forEach {
                if (!addedNames.contains(it.customerAccount)) {
                    latestRecordsList.add(it)
                    addedNames.add(it.customerAccount)
                }
            }
            return latestRecordsList
        }

        fun getLastDue(name: String, useCache: Boolean = true): String {
            val customerRecords = getAllLatestRecords(useCache)
            customerRecords.forEach {
                if (it.name == name)
                    return it.balanceDue
            }
            return "0"
        }

        private var recordsKey = "customerRecords"
        fun getRecords(useCache: Boolean = true): ArrayList<CustomerData> {
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<ArrayList<CustomerData>>(AppContexts.get(), recordsKey, useCache)
            LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
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
                .sheetId(ProjectConfig.get_db_finalize_sheet_id())
                .tabName(FinalizeConfig.SHEET_FINALIZE_DELIVERIES_TAB_NAME)
                .build().execute()

            val recordsList = result.parseToObject<CustomerData>(result.getRawResponse(), object : TypeToken<ArrayList<CustomerData>?>() {}.type)
            recordsList.sortBy { it.orderId }
            recordsList.reverse()
            return recordsList
        }

        fun getCustomerDefaultRate(name: String): Int {
            return SingleAttributedData.getFinalRateInt() + SingleAttributedData.getBufferRateInt() + CustomerKYC.get(name)!!.rateDifference.toInt()
        }

        fun getDeliveryRate(name: String): Int {
            val customerDeliveryRateFromSavedDeliveredData = getCustomerDeliveryRateFromSavedDeliveredData(name)

            return if (customerDeliveryRateFromSavedDeliveredData > 0) customerDeliveryRateFromSavedDeliveredData
            else getCustomerDefaultRate(name)
        }

        private fun getCustomerDeliveryRateFromSavedDeliveredData(name: String): Int {
            return try {
                when {
                    NumberUtils.getIntOrZero(DeliverToCustomerActivity.getDeliveryRecord(name)!!.rate) > 0 -> NumberUtils.getIntOrZero(DeliverToCustomerActivity.getDeliveryRecord(name)!!.rate)
                    else -> 0
                }
            } catch (e: NullPointerException) {
                0
            }
        }

        fun getAllCustomerNames(): List<String> {
            return getRecords().stream()
                .filter { d -> d.name.isNotEmpty() }
                .map(CustomerData::name)
                .collect(Collectors.toSet()).toList().sorted()
        }
    }
}