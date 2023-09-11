package com.tech4bytes.mbrosv3.Loading

import android.view.View
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.tech4bytes.mbrosv3.R
import kotlin.reflect.KMutableProperty1

data class LoadModel(
    var id: String,
    var requiredKg: String,
    var requiredPc: String,
    var actualPc: String = "",
    var actualKg: String = "",
    var loadingStatus: String = "",
    var farmRate: String = "",
    var bufferPrice: String = "",
) : java.io.Serializable {

    fun isDone(): Boolean {
        return this.loadingStatus == LoadConfig.string_tag__loadingStatus_completed
    }

    companion object {
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
                LoadModel::actualPc -> view.findViewById<TextInputEditText>(R.id.activity_delivering_load_actualPc)
                LoadModel::actualKg -> view.findViewById<TextInputEditText>(R.id.activity_delivering_load_actualKg)
                else -> null!!
            }
        }
    }
}