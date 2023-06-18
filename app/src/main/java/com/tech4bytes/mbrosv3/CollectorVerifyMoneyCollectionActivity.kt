package com.tech4bytes.mbrosv3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils

class CollectorVerifyMoneyCollectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_verify_money_collection)
        AppContexts.set(this, this)
        AppUtils.logError()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        showDeliveryData()
    }

    class VerifyElements {
        var kgPc: Boolean = false
        var paidAmount: Boolean = false
    }

    var map: MutableMap<String, VerifyElements> = mutableMapOf()

    private fun showDeliveryData() {
        var deliveredData = DeliverToCustomerDataHandler.get()
        var count = 0
        var bundlesCount = 0
        deliveredData = ListUtils.sortListByAttribute(deliveredData, DeliverToCustomerDataModel::id)
        deliveredData.forEach { deliveryEntry ->
            map[deliveryEntry.name] = VerifyElements()
            count++
            val listContainer = findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_container)
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_collector_verify_money_collection_entries, null)
            val amountPaidField = entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_paid_amount)
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_seq_no).text = "$count."
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_name).text = deliveryEntry.name
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_order_pc).text = deliveryEntry.deliveredKg
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_order_kg).text = deliveryEntry.deliveredPc
            amountPaidField.text = deliveryEntry.paid
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_total_due_amount).text = deliveryEntry.balanceDue
            updateColors(entry, NumberUtils.getIntOrZero(amountPaidField.text.toString()), map[deliveryEntry.name]!!.kgPc, map[deliveryEntry.name]!!.paidAmount)

            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container).setOnClickListener {
                map[deliveryEntry.name]!!.kgPc = !map[deliveryEntry.name]!!.kgPc
                map[deliveryEntry.name]!!.paidAmount = !map[deliveryEntry.name]!!.paidAmount
                updateColors(entry, NumberUtils.getIntOrZero(amountPaidField.text.toString()), map[deliveryEntry.name]!!.kgPc, map[deliveryEntry.name]!!.paidAmount)
            }

            listContainer.addView(entry)

            if(NumberUtils.getIntOrZero(deliveryEntry.paid) > 0) {
                bundlesCount++
            }
        }

        updateSummary(bundlesCount)
    }

    private fun updateSummary(bundlesCount: Int) {
        val bundles = findViewById<TextView>(R.id.vmc_number_of_money_bundles)
        bundles.text = bundlesCount.toString()
    }

    private fun updateColors(entry: View, paidAmount: Int, isKgPcVerified: Boolean, isPaidAmountVerified: Boolean) {
        if (paidAmount == 0 || (isKgPcVerified && isPaidAmountVerified)) {
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
        } else {
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_not_valid))
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}