package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class GetOrdersFinalize : AppCompatActivity() {

    private lateinit var listOrders: List<GetCustomerOrders>

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
            if ((it.orderedPc.isNotEmpty() && it.orderedPc.toInt() > 0) || (it.orderedKg.isNotEmpty() && it.orderedKg.toInt() > 0)) {
                addEntry(it)
            }
        }
    }

    private fun addEntry(order: GetCustomerOrders) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_get_orders_finalize_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_orders_finalize_fragments, null)
        val entryInitialPcAttr = entry.findViewById<TextView>(R.id.fragment_customer_order_pc)
        val entryInitialKgAttr = entry.findViewById<TextView>(R.id.fragment_customer_order_kg)
        val entryNameAttr = entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name)
        val entryFinalPcAttr = entry.findViewById<EditText>(R.id.finalize_order_fragment_finalizePc)
        val entryFinalKgAttr = entry.findViewById<EditText>(R.id.finalize_order_fragment_finalizeKg)

        val finalOrderPc: Double
        val finalOrderKg: Double

        if (order.orderedPc.isNotEmpty()) {
            finalOrderPc = NumberUtils.getDoubleOrZero(order.orderedPc)
        } else {
            finalOrderPc = NumberUtils.getDoubleOrZero(order.orderedKg) / NumberUtils.getIntOrZero(SingleAttributedData.getRecords().estimatedLoadAvgWt) / 1000
            entryInitialPcAttr.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
        }

        if (order.orderedKg.isNotEmpty()) {
            finalOrderKg = NumberUtils.getDoubleOrZero(order.orderedKg)
        } else {
            finalOrderKg = NumberUtils.getDoubleOrZero((order.orderedPc.toInt() * (SingleAttributedData.getRecords().estimatedLoadAvgWt.toInt() / 1000)).toString(), "#.#")
            entryInitialKgAttr.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
        }

        UIUtils.setUIElementValue(entryNameAttr, order.name)
        UIUtils.setUIElementValue(entryInitialPcAttr, finalOrderPc.toString())
        UIUtils.setUIElementValue(entryInitialKgAttr, finalOrderKg.toString())

        entryFinalPcAttr.hint = finalOrderPc.toString()
        entryFinalKgAttr.hint = finalOrderKg.toString()

        if (order.calculatedKg.isNotEmpty())
            entryFinalKgAttr.setText(order.calculatedKg)

        if (order.calculatedPc.isNotEmpty())
            entryFinalPcAttr.setText(order.calculatedPc)

        entryFinalPcAttr.doOnTextChanged { _, _, _, _ ->
            order.calculatedPc = entryFinalPcAttr.text.toString().replace(" ", "")
            updatePcs()
            localSave()
        }

        entryFinalKgAttr.doOnTextChanged { _, _, _, _ ->
            order.calculatedKg = entryFinalKgAttr.text.toString().replace(" ", "")
            updateKgs()
            localSave()
        }

        listContainer.addView(entry)
    }

    private fun updateKgs() {
        var sum = 0.0
        listOrders.forEach {
            sum += try {
                it.calculatedKg.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        }
        val finalizeKg = findViewById<TextView>(R.id.orders_finalize_kgs)
        finalizeKg.text = sum.toString()
    }

    private fun updatePcs() {
        var sum = 0
        listOrders.forEach {
            sum += try {
                it.calculatedPc.toInt()
            } catch (e: NumberFormatException) {
                0
            }
        }
        val finalizePc = findViewById<TextView>(R.id.orders_finalize_pcs)
        finalizePc.text = sum.toString()
    }

    private fun localSave() {
        GetCustomerOrders.saveToLocal()
    }

    fun onClickGoToGetOrdersPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    fun goToOrdersMakeList(view: View) {
        val switchActivityIntent = Intent(this, OrdersMakeList::class.java)
        startActivity(switchActivityIntent)
        finish()
    }
}