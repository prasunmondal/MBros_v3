package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders

import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

data class DeliverCustomerOrders(var id: String, var Timestamp: String, var name: String, var pc: String, var kg: String, var rate: String, var total: String, var prevDue: String, var totalDue: String, var paid: String, var due: String) {
    
    companion object {
        fun <T> get(useCache: Boolean = true): List<T> {
            val cacheResults = CentralCache.get<ArrayList<T>>(AppContexts.get(), DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, useCache)

            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer<T>()
                CentralCache.put(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME, resultFromServer)
                resultFromServer
            }
        }

        fun save(objects: List<DeliverCustomerOrders>) {
            saveObjectsToServer(objects)
            saveToLocal(objects)
        }

        fun deleteAll() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
            saveToLocal(listOf())
        }

        private fun <T> saveObjectsToServer(objects: List<T>) {
            objects.forEach {
                PostObject.builder()
                    .scriptId(ProjectConfig.dBServerScriptURL)
                    .sheetId(ProjectConfig.DB_SHEET_ID)
                    .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                    .dataObject(it as Any)
                    .build().execute()
            }
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