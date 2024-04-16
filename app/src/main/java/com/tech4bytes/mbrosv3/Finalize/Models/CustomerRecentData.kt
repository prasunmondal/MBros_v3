package com.tech4bytes.mbrosv3.Finalize.Models

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.*
import java.util.stream.Collectors

object CustomerRecentData : Tech4BytesSerializable<CustomerData>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_finalize_sheet_id(),
    "deliveries",
    "=QUERY(IMPORTRANGE(\"https://docs.google.com/spreadsheets/d/11TA2pPlxqajVwkPEigNMPNfsV-12CExxmySk1OMw_v8\",\"deliveries!A2:Az\"), \n" +
            "\"select * where \"&\" Col1 =\"&TEXTJOIN(\" or Col1=\",true,QUERY(IMPORTRANGE(\"https://docs.google.com/spreadsheets/d/11TA2pPlxqajVwkPEigNMPNfsV-12CExxmySk1OMw_v8\",\"deliveries!A2:Az\"), \"select max( Col1 ) group by Col3 label max( Col1 ) ''\"))&\"\"&\"\")",
    object : TypeToken<ArrayList<CustomerData>?>() {}.type,
    appendInServer = true,
    appendInLocal = true,
    cacheTag = "recentData"
) {
    fun getCustomerNames(): HashSet<String> {
        val customerNames: HashSet<String> = hashSetOf()
        get().forEach {
            customerNames.add(it.name)
        }
        return customerNames
    }

    fun spoolDeliveringData() {
        var deliveredData = DeliverToCustomerDataHandler.get()
        deliveredData = Sorter.sortByNameList(deliveredData, DeliverToCustomerDataModel::name) as List<DeliverToCustomerDataModel>

        val totalProfit = DaySummaryUtils.getDayProfit()
        LogMe.log("Total Profit: $totalProfit")
        var actualDeliveredKg = 0.0
        deliveredData.forEach {
            actualDeliveredKg += NumberUtils.getDoubleOrZero(it.deliveredKg)
        }
        deliveredData.forEach {
            it.timestamp = DateUtils.getDateInFormat(Date(it.id.toLong()), "M/d/yyyy")
            val record = CustomerData(it, actualDeliveredKg, totalProfit)
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
        var customerRecords = get(useCache).sortedBy { it.orderId }.reversed()
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
//        var customerRecords = Get.builder().scriptId(scriptURL).sheetId(ProjectConfig.get_db_finalize_sheet_id()).tabName(tabname)
//            .query("=QUERY(IMPORTRANGE(\"https://docs.google.com/spreadsheets/d/11TA2pPlxqajVwkPEigNMPNfsV-12CExxmySk1OMw_v8\",\"deliveries!A2:Az\"), \n" +
//                    "\"select * where \"&\" Col1 =\"&TEXTJOIN(\" or Col1=\",true,QUERY(IMPORTRANGE(\"https://docs.google.com/spreadsheets/d/11TA2pPlxqajVwkPEigNMPNfsV-12CExxmySk1OMw_v8\",\"deliveries!A2:Az\"), \"select max( Col1 ) group by Col3 label max( Col1 ) ''\"))&\"\"&\"\")").execute()
        var customerRecords = get(useCache)
        LogMe.log("Length before filtering: " + customerRecords.size)
        customerRecords = customerRecords.sortedBy { it.orderId }
        customerRecords = customerRecords.reversed()

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

    fun getCustomerDefaultRate(name: String): Int {
        return SingleAttributedDataUtils.getFinalRateInt() + SingleAttributedDataUtils.getBufferRateInt() + CustomerKYC.getByName(name)!!.rateDifference.toInt()
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
        return get(useCache).stream()
            .filter { d -> d.name.isNotEmpty() }
            .map(CustomerData::name)
            .collect(Collectors.toSet()).toList().sorted()
    }
}
