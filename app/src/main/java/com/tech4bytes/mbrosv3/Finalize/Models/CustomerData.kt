package com.tech4bytes.mbrosv3.Finalize.Models

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.Date
import java.util.stream.Collectors

class CustomerData : java.io.Serializable {
    var orderId = ""
    var timestamp = ""
    var name = ""
    var deliveredPc = ""
    var deliveredKg = ""
    var rate = ""
    var prevAmount = ""
    var deliverAmount = ""
    var totalAmount = ""
    var paidCash = ""
    var paidOnline = ""
    var paid = ""
    var customerAccount = ""
    var khataBalance = ""
    var avgWt = ""
    var profit = ""
    var profitPercent = ""
    var notes = ""
    var discount = ""
    var otherBalances = ""
    var totalBalance = ""
    var khataDue = ""

    constructor(deliveryObj: DeliverToCustomerDataModel, totalProfit: Double, totalKgsDelivered: Int) {

        val profitByCustomer = totalProfit * NumberUtils.getDoubleOrZero(deliveryObj.deliveredKg) / totalKgsDelivered
        val profitPercentByCustomer = profitByCustomer / totalProfit * 100
        LogMe.log("ProfitPerCustomer: Name: ${deliveryObj.name}: $totalProfit * ${NumberUtils.getDoubleOrZero(deliveryObj.deliveredKg)} / $totalKgsDelivered = $profitByCustomer")

        this.orderId = deliveryObj.id
        this.timestamp = deliveryObj.timestamp
        this.name = deliveryObj.name
        this.deliveredPc = deliveryObj.deliveredPc
        this.deliveredKg = deliveryObj.deliveredKg
        this.rate = deliveryObj.rate
        this.prevAmount = deliveryObj.prevDue
        this.deliverAmount = deliveryObj.deliverAmount
        this.khataDue = deliveryObj.khataBalance
        this.paidCash = deliveryObj.paidCash
        this.paidOnline = deliveryObj.paidOnline
        this.paid = deliveryObj.paid
        this.customerAccount = deliveryObj.customerAccount
        this.khataBalance = deliveryObj.khataBalance
        this.otherBalances = deliveryObj.otherBalances
        this.totalBalance = deliveryObj.totalBalance
        this.avgWt = NumberUtils.roundOff3places(deliveredKg.toDouble() / deliveredPc.toInt()).toString()
        this.profit = NumberUtils.roundOff2places(profitByCustomer).toString()
        this.profitPercent = NumberUtils.roundOff2places(profitPercentByCustomer).toString()
        this.notes = deliveryObj.notes
    }

    override fun toString(): String {
        return "CustomerData(orderId='$orderId', timestamp='$timestamp', name='$name', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', prevAmount='$prevAmount', deliveredAmount='$deliverAmount', totalAmount='$totalAmount', paid='$paid', balanceDue='$khataBalance', avgWt='$avgWt', profit='$profit', profitPercent='$profitPercent')"
    }
}

object CustomerDataUtils : GSheetSerialized<CustomerData>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURL,
    sheetId = ProjectConfig.get_db_finalize_sheet_id(),
    tabName = "deliveries",
    modelClass = CustomerData::class.java
) {
    fun getCustomerNames(): HashSet<String> {
        return fetchAll().execute().map { it.name }.toHashSet()
    }

    fun spoolDeliveringData() {
        var deliveredData = DeliverToCustomerDataHandler.fetchAll().execute()
        deliveredData = Sorter.sortByNameList(deliveredData, DeliverToCustomerDataModel::name) as List<DeliverToCustomerDataModel>

        val totalProfit = DaySummaryUtils.getDayProfit()
        LogMe.log("Total Profit: $totalProfit")
        var actualDeliveredKg = 0.0
        deliveredData.forEach {
            actualDeliveredKg += NumberUtils.getDoubleOrZero(it.deliveredKg)
        }

        val finalizeDeliveryObjs = mutableListOf<CustomerData>()
        deliveredData.forEach {
            it.timestamp = DateUtils.getDateInFormat(Date(it.id.toLong()), "M/d/yyyy")
            finalizeDeliveryObjs.add(CustomerData(it, actualDeliveredKg, totalProfit))
        }
        CustomerRecentData.insert(finalizeDeliveryObjs).queue()
    }

    fun getCustomerDefaultRate(name: String): Int {
        return SingleAttributedDataUtils.getFinalRateInt() + SingleAttributedDataUtils.getBufferRateInt() + NumberUtils.getIntOrZero(CustomerKYC.getByName(name)!!.rateDifference)
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

    fun getAllCustomerNames(useCache: Boolean = true): List<String> {
        val customersFromCustomerDetails = CustomerKYC.fetchAll(useCache).execute().stream()
            .filter { d -> d.nameEng.isNotEmpty() }
            .map(CustomerKYCModel::nameEng)
            .collect(Collectors.toSet()).toList()

        val customersFromFinalizedDeliveries = fetchAll(useCache).execute().stream()
            .filter { d -> d.name.isNotEmpty() }
            .map(CustomerData::name)
            .collect(Collectors.toSet()).toList()

        return (customersFromCustomerDetails + customersFromFinalizedDeliveries).toSet().toList().sorted()
    }
}
