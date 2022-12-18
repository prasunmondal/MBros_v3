package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class ActivityDeliveringListOrders : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_list)
        AppContexts.set(this, this)

        showOrders()
    }

    fun showOrders() {
        var orders = GetCustomerOrders.get()
        orders.forEach { order ->
            LogMe.log(order.toString())

            val listContainer = findViewById<LinearLayout>(R.id.activity_delivering_deliver_order_list)
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_delivering_list_fragment_order_details, null)

            val seqNoElement = entry.findViewById<AppCompatTextView>(R.id.activity_delivering_deliver_fragment_customer_seq_no)
            val nameElement = entry.findViewById<AppCompatTextView>(R.id.activity_delivering_deliver_fragment_customer_name)
            val pcElement = entry.findViewById<AppCompatTextView>(R.id.activity_delivering_deliver_fragment_order_pc)
            val kgElement = entry.findViewById<AppCompatTextView>(R.id.activity_delivering_deliver_fragment_order_kg)

            UIUtils.setUIElementValue(this, seqNoElement, order.seqNo)
            UIUtils.setUIElementValue(this, nameElement, order.name)
            UIUtils.setUIElementValue(this, pcElement, order.estimatePc)
            UIUtils.setUIElementValue(this, kgElement, order.estimateKg)

            listContainer.addView(entry)
        }

    }

//    private fun createEstimatesView(order: GetCustomerOrders) {
//
//        val layoutInflater = LayoutInflater.from(AppContexts.get())
//        val entry = layoutInflater.inflate(R.layout.activity_get_order_estimates_fragment_customer_order, null)
//
//        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_sl_no), order.seqNo)
//        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name), order.name)
//        UIUtils.setUIElementValue(this, pcElement, order.estimatePc)
//        UIUtils.setUIElementValue(this, kgElement, order.estimateKg)
//        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_rate), order.rate)
//
//        pcElement.doOnTextChanged { text, start, before, count ->
//            updateTotalPc()
//        }
//
//        kgElement.doOnTextChanged { text, start, before, count ->
//            updateTotalKg()
//        }
//
//        val deleteBtn = entry.findViewById<ImageButton>(R.id.fragment_customer_order_delete_record_button)
//        deleteBtn.setOnClickListener {
//            uiEntriesList.remove(entry)
//            listContainer.removeView(entry)
//            updateTotalPc()
//            updateTotalKg()
//        }
//
//        uiEntriesList.add(entry)
//        listContainer.addView(entry)
//    }
}