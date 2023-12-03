package com.tech4bytes.mbrosv3.Finalize.Models

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCUtils
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

class CustomerDataUtils {

    companion object {
        fun getCustomerDefaultRate(name: String): Int {
            return SingleAttributedDataUtils.getFinalRateInt()
            + SingleAttributedDataUtils.getBufferRateInt()
            + CustomerKYCUtils.getCustomerByEngName(name)!!.rateDifference.toInt()
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

        fun getLastDue(name: String): String {
            val customerRecords = CustomerDataUtils.getAllLatestRecords()
            customerRecords.forEach {
                if (it.name == name)
                    return it.balanceDue
            }
            return "0"
        }

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
                val record = CustomerData(it.id, it.timestamp, it.name, it.deliveredPc, it.deliveredKg, it.rate, it.prevDue, it.todaysAmount, it.totalDue, it.paid, it.balanceDue, NumberUtils.roundOff2places(profitByCustomer).toString(), NumberUtils.roundOff2places(profitPercentByCustomer).toString())
                CustomerData.addToFinalizeSheet(record)
            }
        }

        fun getAllLatestRecords(): MutableList<CustomerData> {
            val customerRecords = CustomerData.getRecords()
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
    }
}