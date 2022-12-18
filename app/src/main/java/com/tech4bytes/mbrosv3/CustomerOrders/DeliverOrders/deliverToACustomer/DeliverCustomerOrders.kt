package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.view.View
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import kotlin.reflect.KMutableProperty1

data class DeliverCustomerOrders(
    var id: String,
    var timestamp: String,
    var name: String,
    var orderedPc: String,
    var orderedKg: String,
    var deliveredPc: String,
    var deliveredKg: String,
    var rate: String,
    var todaysAmount: String,
    var prevDue: String,
    var totalDue: String,
    var paid: String,
    var balanceDue: String,
    var deliveryStatus: String): java.io.Serializable {
    
    companion object {

        fun getByName(inputName: String): DeliverCustomerOrders? {
            DeliverCustomerOrders.get().forEach {
                if(it.name == inputName) {
                    return it
                }
            }
            return null
        }

        fun get(useCache: Boolean = true): MutableList<DeliverCustomerOrders> {
            val cacheResults = CentralCache.get<ArrayList<DeliverCustomerOrders>>(AppContexts.get(), DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer<DeliverCustomerOrders>()
                CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, resultFromServer)
                resultFromServer as MutableList
            }
        }

        fun save(obj: DeliverCustomerOrders) {
            saveObjectToServer(obj)
            val tempList = get()
            tempList.add(obj)
            saveToLocal(tempList)
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

        private fun <T> saveObjectToServer(obj: T) {
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

        private fun <T> getFromServer(): List<T> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()

            return result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<T>?>() {}.type
            )
        }
    }
}