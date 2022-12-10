package com.tech4bytes.mbrosv3.GetOrders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe


class ActivityGetOrderEstimates : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_order_estimates)
        AppContexts.set(this, this)
        populateCustomerList()
    }

    private fun createEstimatesView(customerName: String) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_get_order_estimates__order_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_order_estimates_fragment_customer_order, null)

        entry.findViewById<TextView>(R.id.fragment_customer_order_name).text = customerName
        listContainer.addView(entry)
    }

    private fun getCustomerNamesAsStringList(): List<String> {
        val namesList = mutableListOf<String>()

        CustomerKYC.getAllCustomers().forEach {
            namesList.add(it.getDisplayName())
        }
        return namesList
    }

    @SuppressLint("ClickableViewAccessibility")
    fun populateCustomerList() {
        val dropDown = findViewById<MaterialAutoCompleteTextView>(R.id.activity_get_order_estimates__customer_selection_dropdown)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(AppContexts.get(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, getCustomerNamesAsStringList())
        (dropDown as MaterialAutoCompleteTextView).setAdapter(adapter)

        dropDown.setOnTouchListener { _, _ ->
                dropDown.showDropDown()
                dropDown.requestFocus()
                false
            }

        dropDown.setOnItemClickListener { parent, arg1, position, arg3 ->
            val item = parent.getItemAtPosition(position)
            LogMe.log("Selected Customer: ${item}")
            createEstimatesView(item.toString())
        }
    }

    fun deleteAll() {

    }
}