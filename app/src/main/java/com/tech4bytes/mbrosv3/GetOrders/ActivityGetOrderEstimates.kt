package com.tech4bytes.mbrosv3.GetOrders

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class ActivityGetOrderEstimates : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_order_estimates)
        AppContexts.set(this, this)
        populateCustomerList()
    }

//    fun createEstimatesView() {
//        ExpenseData.get_filterable_attributes().forEach {
//            val layoutInflater = LayoutInflater.from(this)
//            val item: View = layoutInflater.inflate(R.layout.fragment_popup_menu_item, null)
//        }
//
//    }

    fun getCustomerNames(): ArrayList<String> {
        var list: ArrayList<String> = ArrayList()
        list.add("Prasun")
        list.add("Mondal")
        return list
    }
    fun populateCustomerList() {
        val dropDown = findViewById<MaterialAutoCompleteTextView>(R.id.activity_get_order_estimates__customer_selection_dropdown)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(AppContexts.get(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, getCustomerNames())
        (dropDown as MaterialAutoCompleteTextView).setAdapter(adapter)

        dropDown.setOnTouchListener { _, _ ->
                dropDown.showDropDown()
                dropDown.requestFocus()
                false
            }
    }

    fun deleteAll() {

    }
}