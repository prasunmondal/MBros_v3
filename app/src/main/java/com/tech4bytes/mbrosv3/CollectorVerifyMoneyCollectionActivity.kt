package com.tech4bytes.mbrosv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils

class CollectorVerifyMoneyCollectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_verify_money_collection)
        AppContexts.set(this, this)
        AppUtils.logError()
        window.addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        showDeliveryData()
    }

    class VerifyElements {
        var kgPc: Boolean = false
        var paidAmount: Boolean = false
    }

    var map: MutableMap<String, VerifyElements> = mutableMapOf()

    fun showDeliveryData() {
        var deliveredData = DeliverCustomerOrders.get()
        var count = 0
        deliveredData = ListUtils.sortListByAttribute(deliveredData, DeliverCustomerOrders::id)
        deliveredData.forEach { deliveryEntry ->
            map[deliveryEntry.name] = VerifyElements()
            count++
            val listContainer = findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_container)
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_collector_verify_money_collection_entries, null)

            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_seq_no).text = "$count."
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_name).text = deliveryEntry.name
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_order_pc).text = deliveryEntry.deliveredKg
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_order_kg).text = deliveryEntry.deliveredPc
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_paid_amount).text = deliveryEntry.paid
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_total_due_amount).text = deliveryEntry.balanceDue

            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container).setOnClickListener {
                map[deliveryEntry.name]!!.kgPc = !map[deliveryEntry.name]!!.kgPc
                map[deliveryEntry.name]!!.paidAmount = !map[deliveryEntry.name]!!.paidAmount
                updateColors(entry, map[deliveryEntry.name]!!.kgPc, map[deliveryEntry.name]!!.paidAmount)
            }

            listContainer.addView(entry)
        }
    }

    fun updateColors(entry: View, isKgPcVerified: Boolean, isPaidAmountVerified: Boolean) {
        if(isKgPcVerified && isPaidAmountVerified) {
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
        } else {
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_not_valid))
        }
    }

    fun activity_collector_verify_money_collection_sync_data_btn(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
        finish()
    }
}