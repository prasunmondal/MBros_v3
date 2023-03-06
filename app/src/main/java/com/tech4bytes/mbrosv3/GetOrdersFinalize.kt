package com.tech4bytes.mbrosv3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
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

        val orderedPcElement = entry.findViewById<TextView>(R.id.fragment_customer_order_pc)
        val orderedKgElement = entry.findViewById<TextView>(R.id.fragment_customer_order_kg)
        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name), order.name)
        if(order.orderedPc.isNotEmpty()) {
            UIUtils.setUIElementValue(this, orderedPcElement, order.orderedPc)
        } else {
            UIUtils.setUIElementValue(this, orderedPcElement, (order.orderedKg.toInt()/2).toString())
            orderedPcElement.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
        }

        if(order.orderedKg.isNotEmpty()) {
            UIUtils.setUIElementValue(this, orderedKgElement, order.orderedKg)
        } else {
            UIUtils.setUIElementValue(this, orderedKgElement, (order.orderedPc.toInt() * SingleAttributedData.getRecords().estimatedLoadAvgWt.toInt()/1000).toString())
            orderedKgElement.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
        }

        val finalizePc = entry.findViewById<TextView>(R.id.finalize_order_fragment_finalizePc)
        val finalizeKg = entry.findViewById<TextView>(R.id.finalize_order_fragment_finalizeKg)

        finalizePc.doOnTextChanged { text, start, before, count ->
            finalizeKg.text = try {
                "${finalizePc.text.toString().toInt() * SingleAttributedData.getRecords().estimatedLoadAvgWt.toInt() / 1000}"
            } catch (e: Exception) {
                ""
            }
        }

        listContainer.addView(entry)
    }

    fun onClickGoToGetOrdersPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
    }
}