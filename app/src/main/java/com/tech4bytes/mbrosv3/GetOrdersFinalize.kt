package com.tech4bytes.mbrosv3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.ActivityGetCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class GetOrdersFinalize : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_orders_finalize)
        AppContexts.set(this)

        showList()
    }

    private fun showList() {
//        val listContainer = findViewById<LinearLayout>(R.id.activity_get_orders_finalize_list_container)

        GetCustomerOrders.get().forEach {
            if((it.orderedPc.isNotEmpty() && it.orderedPc.toInt() > 0) || (it.orderedKg.isNotEmpty() && it.orderedKg.toInt() > 0)) {
                addEntry(it)
            }
        }

    }

    private fun addEntry(order: GetCustomerOrders) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_get_orders_finalize_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_orders_finalize_fragments, null)

        val pcElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc)
        val kgElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg)
//        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_sl_no), order.seqNo)
        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name), order.name)
        UIUtils.setUIElementValue(this, pcElement, order.orderedPc)
        UIUtils.setUIElementValue(this, kgElement, order.orderedKg)
//        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_rate), order.rate)

        pcElement.doOnTextChanged { text, start, before, count ->
//            updateTotalPc()
        }

        kgElement.doOnTextChanged { text, start, before, count ->
//            updateTotalKg()
        }

//        uiEntriesList.add(entry)
        listContainer.addView(entry)
    }

    fun onClickGoToGetOrdersPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
    }
}