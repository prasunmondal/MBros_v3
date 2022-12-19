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

data class LoadModel(var id: String,
                     var requiredKg: String,
                     var requiredPc: String,
                     var actualPc: String = "",
                     var actualKg: String = "",
                     var loadingStatus: String = ""
): java.io.Serializable {

    fun isDone(): Boolean {
        return this.loadingStatus == LoadConfig.string_tag__loadingStatus_completed
    }

    companion object {
        fun get(useCache: Boolean = true): LoadModel {
            val cacheResults = CentralCache.get<LoadModel>(AppContexts.get(), LoadConfig.SHEET_TO_LOAD_DATA, useCache)

            // TODO: Get the latest record after sorting by IDs
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(LoadConfig.SHEET_TO_LOAD_DATA, resultFromServer)
                resultFromServer
            }
        }

        fun save(obj: LoadModel) {
            obj.loadingStatus = LoadConfig.string_tag__loadingStatus_completed
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
                .tabName(LoadConfig.SHEET_TO_LOAD_DATA)
                .build().execute()
            saveToLocal(null)
        }

        fun getUiElementFromOrderingPage(view: View, attribute: KMutableProperty1<LoadModel, *>): View {
            return when (attribute) {
                LoadModel::requiredKg -> view.findViewById<TextView>(R.id.activity_get_order_estimates__load_kg)
                LoadModel::requiredPc -> view.findViewById<TextView>(R.id.activity_get_order_estimates__load_pc)
                else -> null!!
            }
        }

        fun getUiElementFromLoadingPage(view: View, attribute: KMutableProperty1<LoadModel, *>): View {
            return when (attribute) {
                LoadModel::requiredPc -> view.findViewById<TextView>(R.id.activity_delivering_load_requiredPc)
                LoadModel::requiredKg -> view.findViewById<TextView>(R.id.activity_delivering_load_requiredKg)
                LoadModel::actualPc -> view.findViewById<EditText>(R.id.activity_delivering_load_actualPc)
                LoadModel::actualKg -> view.findViewById<EditText>(R.id.activity_delivering_load_actualKg)
                else -> null!!
            }
        }

        private fun saveObjectsToServer(objects: LoadModel) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(LoadConfig.SHEET_TO_LOAD_DATA)
                .dataObject(objects as Any)
                .build().execute()
        }

        private fun getObjectFromUI(view: View): LoadModel {
            val id = System.currentTimeMillis()
            return LoadModel(id.toString(),
                UIUtils.getUIElementValue(getUiElementFromOrderingPage(view, LoadModel::requiredKg)),
                UIUtils.getUIElementValue(getUiElementFromOrderingPage(view, LoadModel::requiredPc)))
        }

        private fun saveToLocal(objects: LoadModel?) {
            CentralCache.put(LoadConfig.SHEET_TO_LOAD_DATA, objects)
        }

        private fun getFromServer(): LoadModel {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(LoadConfig.SHEET_TO_LOAD_DATA)
                .build().execute()

            val list: ArrayList<LoadModel>? = result.parseToObject(result.getRawResponse(),
                object : TypeToken<ArrayList<LoadModel>?>() {}.type
            )

            return if(list !=null && list.isNotEmpty()) list[list.size - 1] else LoadModel("","","")
        }
    }
}