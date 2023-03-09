package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

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
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class GetOrdersFinalize : AppCompatActivity() {

    lateinit var listOrders: List<GetCustomerOrders>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_orders_finalize)
        AppContexts.set(this)

        listOrders = GetCustomerOrders.get()
        LogMe.log(listOrders.toString())
        showList()
        updatePcs()
        updateKgs()
    }

    private fun showList() {
        listOrders.forEach {
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

        if(order.calculatedKg.isNotEmpty())
            finalizeKg.text = order.calculatedKg

        if(order.calculatedPc.isNotEmpty())
            finalizePc.text = order.calculatedPc

        finalizePc.doOnTextChanged { text, start, before, count ->
            order.calculatedPc = finalizePc.text.toString().replace(" ","")
            updatePcs()
            localSave()
        }

        finalizeKg.doOnTextChanged { text, start, before, count ->
            order.calculatedKg = finalizeKg.text.toString().replace(" ","")
            updateKgs()
            localSave()
        }

        listContainer.addView(entry)
    }

    private fun updateKgs() {
        var sum = 0.0
        listOrders.forEach { sum += try {it.calculatedKg.toDouble() } catch (e: NumberFormatException) {0.0} }
        val finalizeKg = findViewById<TextView>(R.id.orders_finalize_kgs)
        finalizeKg.text = sum.toString()
    }

    private fun updatePcs() {
        var sum = 0
        listOrders.forEach { sum += try {it.calculatedPc.toInt() } catch (e: NumberFormatException) {0} }
        val finalizePc = findViewById<TextView>(R.id.orders_finalize_pcs)
        finalizePc.text = sum.toString()
    }

    private fun localSave() {
        GetCustomerOrders.saveToLocal()
    }

    fun onClickGoToGetOrdersPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
    }
}