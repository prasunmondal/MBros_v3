package com.tech4bytes.mbrosv3.GetOrders

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
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import kotlin.reflect.KMutableProperty1

data class OrdersTotalModel(var id: String, var kg: String, var pc: String) {

    companion object {
        fun get(useCache: Boolean = true): OrdersTotalModel {
            val cacheResults = CentralCache.get<OrdersTotalModel>(AppContexts.get(), GetOrdersConfig.SHEET_TO_LOAD_DATA, useCache)

            // TODO: Get the latest record after sorting by IDs
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(GetOrdersConfig.SHEET_TO_LOAD_DATA, resultFromServer)
                resultFromServer
            }
        }

        fun save(obj: OrdersTotalModel) {
            saveObjectsToServer(obj)
            saveToLocal(obj)
        }

        fun save(view: View) {
            val obj = getObjectFromUI(view)
            save(obj)
        }

        fun deleteAll() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(GetOrdersConfig.SHEET_TO_LOAD_DATA)
                .build().execute()
            saveToLocal(null)
        }

        fun getUiElement(view: View, attribute: KMutableProperty1<OrdersTotalModel, *>): View {
            return when (attribute) {
                OrdersTotalModel::kg -> view.findViewById<TextView>(R.id.activity_get_order_estimates__load_kg)
                OrdersTotalModel::pc -> view.findViewById<TextView>(R.id.activity_get_order_estimates__load_pc)
                else -> null!!
            }
        }

        private fun saveObjectsToServer(objects: OrdersTotalModel) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(GetOrdersConfig.SHEET_TO_LOAD_DATA)
                .dataObject(objects as Any)
                .build().execute()
        }

        private fun getObjectFromUI(view: View): OrdersTotalModel {
            val id = System.currentTimeMillis()
            return OrdersTotalModel(id.toString(),
                UIUtils.getUIElementValue(getUiElement(view, OrdersTotalModel::kg)),
                UIUtils.getUIElementValue(getUiElement(view, OrdersTotalModel::pc)))
        }

        private fun saveToLocal(objects: OrdersTotalModel?) {
            CentralCache.put(GetOrdersConfig.SHEET_TO_LOAD_DATA, objects)
        }

        private fun getFromServer(): OrdersTotalModel {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(GetOrdersConfig.SHEET_TO_LOAD_DATA)
                .build().execute()

            val list: ArrayList<OrdersTotalModel>? = result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<OrdersTotalModel>?>() {}.type
            )

            return if(list !=null && list.isNotEmpty()) list[list.size - 1] else OrdersTotalModel("","","")
        }
    }
}