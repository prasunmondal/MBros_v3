package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class OrdersMakeList : AppCompatActivity() {

    lateinit var listOrders: List<GetCustomerOrders>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_make_list)
        AppContexts.set(this)

        listOrders = GetCustomerOrders.get()
        showList()
    }

    private fun showList() {
        listOrders.forEach {
            if((it.orderedPc.isNotEmpty() && it.orderedPc.toInt() > 0) || (it.orderedKg.isNotEmpty() && it.orderedKg.toInt() > 0)) {
                addEntry(it)
            }
        }
    }

    private fun addEntry(order: GetCustomerOrders) {
        val listContainer = findViewById<LinearLayout>(R.id.order_make_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_orders_make_list_fragments, null)

        val elementPc = entry.findViewById<TextView>(R.id.make_list_fragment_pc)
        val elementKg = entry.findViewById<TextView>(R.id.make_list_fragment_kg)
        val elementName = entry.findViewById<AppCompatTextView>(R.id.make_list_fragment_name)
        val elementDue = entry.findViewById<TextView>(R.id.make_list_fragment_due)

        elementPc.text = order.calculatedPc
        elementKg.text = order.calculatedKg
        elementName.text = order.name
        elementDue.text = CustomerData.getLastDue(order.name)

        listContainer.addView(entry)
    }

    fun onClickGoToOrdersGetPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    fun onClickGoToOrdersFinalizePage(view: View) {
        val switchActivityIntent = Intent(this, GetOrdersFinalize::class.java)
        startActivity(switchActivityIntent)
        finish()
    }
}