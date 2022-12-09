package com.tech4bytes.mbrosv3.Loading

import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import com.tech4bytes.mbrosv3.R
import kotlin.reflect.KMutableProperty1

class LoadingDataModel {
    var requiredPc = 0
    var requiredKg = 0.0
    var actualPc = 0
    var actualKg = 0.0
    var avgWeight = 0.0

    companion object {
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
    }
}