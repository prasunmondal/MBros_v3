package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Loading.LoadModel
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.WeightUtils.WeightUtils

class ActivityAdminDeliveryDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_delivery_dashboard)
        AppContexts.set(this)

        updateLoadInfo()
        updateDeliveredInfo()
    }

    fun updateLoadInfo() {
        val totalPcElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_total_pc)
        val totalKgElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_loaded_kg)
        val avgWtElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_total_loaded_avg_wt)

        UIUtils.setUIElementValue(this, totalPcElement, LoadModel.get().actualPc)
        UIUtils.setUIElementValue(this, totalKgElement, LoadModel.get().actualKg)

        try {
            val avgWt = NumberUtils.getDoubleOrZero(LoadModel.get().actualKg) / NumberUtils.getDoubleOrZero(LoadModel.get().actualPc)
            UIUtils.setUIElementValue(this, avgWtElement, "${WeightUtils.roundOff3places(avgWt)}")
        } catch (e: Exception) {
            UIUtils.setUIElementValue(this, avgWtElement, "---")
        }
    }

    fun updateDeliveredInfo() {
        val countersDelivered = DeliverCustomerOrders.get()
        val numberOfCustomersDelivered = countersDelivered.size
        val totalNumberOfCustomers = GetCustomerOrders.getNumberOfCustomersOrdered()
        val deliveredPc = DeliverCustomerOrders.getTotalPcDelivered()
        val deliveredKg = DeliverCustomerOrders.getTotalKgDelivered()
        val avgWt = deliveredKg / deliveredPc

        val deliveredNumberElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_delivered_number)
        val deliveredPcKgElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_delivered_pc_kg)
        val deliveredAvgWtElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_delivered_avg_wt)

        UIUtils.setUIElementValue(this, deliveredNumberElement,
            "$numberOfCustomersDelivered / $totalNumberOfCustomers Counters")
        UIUtils.setUIElementValue(this, deliveredPcKgElement,
            "$deliveredPc pc - $deliveredKg kg")
        UIUtils.setUIElementValue(this, deliveredAvgWtElement,
            "${WeightUtils.roundOff3places(avgWt)} kg/pc")
    }
}