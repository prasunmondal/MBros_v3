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
import com.tech4bytes.mbrosv3.GetOrders.OrdersTotalModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import kotlin.reflect.KMutableProperty1

data class LoadingOrdersTotalModel(var id: String, var kg: String, var pc: String) {

    companion object {
        fun getUiElement(view: View, attribute: KMutableProperty1<OrdersTotalModel, *>): View {
            return when (attribute) {
                OrdersTotalModel::requiredPc -> view.findViewById<TextView>(R.id.activity_delivering_load_requiredPc)
                OrdersTotalModel::requiredKg -> view.findViewById<TextView>(R.id.activity_delivering_load_requiredKg)
                OrdersTotalModel::actualPc -> view.findViewById<EditText>(R.id.activity_delivering_load_actualPc)
                OrdersTotalModel::actualKg -> view.findViewById<EditText>(R.id.activity_delivering_load_actualKg)
                else -> null!!
            }
        }
    }
}