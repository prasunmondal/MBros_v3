package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.app.ProgressDialog
import android.view.View
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import kotlin.reflect.KMutableProperty1

data class DeliverCustomerOrders(
    var id: String = "",
    var date: String = "",
    var timestamp: String = "",
    var name: String = "",
    var orderedPc: String = "",
    var orderedKg: String = "",
    var deliveredPc: String = "",
    var deliveredKg: String = "",
    var rate: String = "",
    var todaysAmount: String = "",
    var prevDue: String = "",
    var totalDue: String = "",
    var paid: String = "",
    var balanceDue: String = "",
    var deliveryStatus: String = ""): java.io.Serializable {
    
    companion object {

        fun getByName(inputName: String): DeliverCustomerOrders? {
            get().forEach {
                if(it.name == inputName) {
                    return it
                }
            }
            return null
        }

        fun get(useCache: Boolean = true): List<DeliverCustomerOrders> {
            var cacheResults = CentralCache.get<List<DeliverCustomerOrders>>(AppContexts.get(), DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            if(cacheResults == null) {
                cacheResults = getFromServer()
                CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, cacheResults)
            }

            return if(cacheResults != null)
                filterToOnlyLatest(cacheResults)
            else
                listOf()
        }

        fun getTotalPcDelivered(): Int {
            var sum = 0
            get().forEach {
                sum += NumberUtils.getIntOrZero(it.deliveredPc)
            }
            return sum
        }

        fun getTotalKgDelivered(): Double {
            var sum = 0.0
            get().forEach {
                sum += NumberUtils.getDoubleOrZero(it.deliveredKg)
            }
            return sum
        }

        fun filterToOnlyLatest(resultFromServer: List<DeliverCustomerOrders>): List<DeliverCustomerOrders> {
            val sorted = ListUtils.sortListByAttribute(resultFromServer, DeliverCustomerOrders::id).reversed()
            val map = mutableMapOf<String, DeliverCustomerOrders>()

            sorted.forEach {
                if(!map.containsKey(it.name)) {
                    map.put(it.name, it)
                }
            }
            return map.values.toList()
        }

        fun save(obj: DeliverCustomerOrders) {
            obj.id = System.currentTimeMillis().toString()
            obj.date = DateUtils.getCurrentTimestamp()
            val tempList = get() + obj
            saveToLocal(tempList)
            saveObjectsToServer(obj)
        }

        fun deleteAll() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
            saveToLocal(listOf())
        }

        fun getUiElementFromDeliveringPage(view: View, attribute: KMutableProperty1<DeliverCustomerOrders, *>): View? {
            return when (attribute) {
                DeliverCustomerOrders::name -> view.findViewById<TextView>(R.id.activity_delivering_deliver_name)
                DeliverCustomerOrders::rate -> view.findViewById<TextView>(R.id.activity_delivering_deliver_rate)
                DeliverCustomerOrders::orderedPc -> view.findViewById<TextView>(R.id.activity_delivering_deliver_ordered_pc)
                DeliverCustomerOrders::orderedKg -> view.findViewById<TextView>(R.id.activity_delivering_deliver_ordered_kg)
                DeliverCustomerOrders::deliveredPc -> view.findViewById<TextView>(R.id.activity_delivering_deliver_delivering_pc)
                DeliverCustomerOrders::deliveredKg -> view.findViewById<TextView>(R.id.activity_delivering_deliver_delivering_kg)
                DeliverCustomerOrders::todaysAmount -> view.findViewById<TextView>(R.id.activity_delivering_deliver_todays_amount)
                DeliverCustomerOrders::prevDue -> view.findViewById<TextView>(R.id.activity_delivering_deliver_prev_due)
                DeliverCustomerOrders::totalDue -> view.findViewById<TextView>(R.id.activity_delivering_deliver_all_total)
                DeliverCustomerOrders::paid -> view.findViewById<TextView>(R.id.activity_delivering_deliver_paid)
                DeliverCustomerOrders::balanceDue -> view.findViewById<TextView>(R.id.activity_delivering_deliver_balance_due)
                else -> null
            }
        }

        private fun <T> saveObjectsToServer(obj: T) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .dataObject(obj as Any)
                .build().execute()
        }

        private fun saveToLocal(objects: List<DeliverCustomerOrders>) {
            CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, objects)
        }

        fun deleteFromLocal() {
            CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, listOf<DeliverCustomerOrders>())
        }

        private fun getFromServer(): List<DeliverCustomerOrders> {
            // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()

            // waitDialog!!.dismiss()
            return result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<DeliverCustomerOrders>?>() {}.type
            )
        }
    }
}