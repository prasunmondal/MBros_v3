package com.tech4bytes.mbrosv3.BusinessData

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import com.tech4bytes.mbrosv3.R
import kotlin.reflect.KMutableProperty1

class SingleAttributedDataUIs {

    companion object {
        fun getUI() {

        }

        fun getUIFromOSDActivity(view: View, attribute: KMutableProperty1<SingleAttributedData, String>): View? {
            return when (attribute) {
                SingleAttributedData::id -> view.findViewById<TextInputLayout>(R.id.osd_farm_rate_container)
                else -> null
            }
        }
    }
}