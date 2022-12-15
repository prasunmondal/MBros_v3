package com.tech4bytes.mbrosv3.Loading

import android.view.View
import android.widget.EditText
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

data class LoadingOrdersTotalModel(var id: String, var kg: String, var pc: String) {

    companion object {
        fun get(useCache: Boolean = true): LoadingOrdersTotalModel {
            val cacheResults = CentralCache.get<LoadingOrdersTotalModel>(AppContexts.get(), LoadingConfig.SHEET_TO_LOAD_DATA, useCache)

            // TODO: Get the latest record after sorting by IDs
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(LoadingConfig.SHEET_TO_LOAD_DATA, resultFromServer)
                resultFromServer
            }
        }

        fun save(obj: LoadingOrdersTotalModel) {
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
                .tabName(LoadingConfig.SHEET_TO_LOAD_DATA)
                .build().execute()
            saveToLocal(null)
        }

        fun getUiElement(view: View, attribute: KMutableProperty1<LoadingDataModel, *>): View {
            return when (attribute) {
                LoadingDataModel::requiredPc -> view.findViewById<TextView>(R.id.activity_delivering_load_requiredPc)
                LoadingDataModel::requiredKg -> view.findViewById<TextView>(R.id.activity_delivering_load_requiredKg)
                LoadingDataModel::actualPc -> view.findViewById<EditText>(R.id.activity_delivering_load_actualPc)
                LoadingDataModel::actualKg -> view.findViewById<EditText>(R.id.activity_delivering_load_actualKg)
                LoadingDataModel::avgWeight -> view.findViewById<TextView>(R.id.activity_delivering_load_actualAvgWt)
                else -> null!!
            }
        }

        private fun saveObjectsToServer(objects: LoadingOrdersTotalModel) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(LoadingConfig.SHEET_TO_LOAD_DATA)
                .dataObject(objects as Any)
                .build().execute()
        }

        private fun getObjectFromUI(view: View): LoadingOrdersTotalModel {
            val id = System.currentTimeMillis()
            return LoadingOrdersTotalModel(id.toString(),
                UIUtils.getUIElementValue(getUiElement(view, LoadingDataModel::requiredKg)),
                UIUtils.getUIElementValue(getUiElement(view, LoadingDataModel::requiredPc)))
        }

        private fun saveToLocal(objects: LoadingOrdersTotalModel?) {
            CentralCache.put(LoadingConfig.SHEET_TO_LOAD_DATA, objects)
        }

        private fun getFromServer(): LoadingOrdersTotalModel {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(LoadingConfig.SHEET_TO_LOAD_DATA)
                .build().execute()

            val list: ArrayList<LoadingOrdersTotalModel>? = result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<LoadingOrdersTotalModel>?>() {}.type
            )

            return if(list !=null && list.isNotEmpty()) list[list.size - 1] else LoadingOrdersTotalModel("","","")
        }
    }
}