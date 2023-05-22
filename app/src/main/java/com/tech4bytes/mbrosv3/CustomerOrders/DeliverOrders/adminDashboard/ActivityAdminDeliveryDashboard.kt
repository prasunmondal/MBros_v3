package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.WeightUtils.WeightUtils
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling

class ActivityAdminDeliveryDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_delivery_dashboard)
        AppContexts.set(this)
        AppUtils.logError()

        updateDashboard(true)
        setCompanyAndRateValuesInUI()
    }

    fun updateLoadInfo(useCache: Boolean) {
        val totalPcElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_total_pc)
        val totalKgElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_loaded_kg)
        val avgWtElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_total_loaded_avg_wt)

        val loadMetadata = SingleAttributedData.getRecords(useCache)
        UIUtils.setUIElementValue(totalPcElement, loadMetadata.actualLoadPc)
        UIUtils.setUIElementValue(totalKgElement, loadMetadata.actualLoadKg)

        try {
            val avgWt = NumberUtils.getDoubleOrZero(loadMetadata.actualLoadKg) / NumberUtils.getDoubleOrZero(loadMetadata.actualLoadPc)
            UIUtils.setUIElementValue(avgWtElement, "${WeightUtils.roundOff3places(avgWt)}")
        } catch (e: Exception) {
            UIUtils.setUIElementValue(avgWtElement, "---")
        }
    }

    fun updateDeliveredInfo(useCache: Boolean) {
        val countersDelivered = DeliverToCustomerDataHandler.get(useCache)
        val numberOfCustomersDelivered = countersDelivered.size
        val totalNumberOfCustomers = GetCustomerOrders.getNumberOfCustomersOrdered(useCache)
        val deliveredPc = DeliverToCustomerCalculations.getTotalPcDelivered()
        val deliveredKg = DeliverToCustomerCalculations.getTotalKgDelivered()
        val avgWt = deliveredKg / deliveredPc

        val deliveredNumberElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_delivered_number)
        val deliveredPcKgElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_delivered_pc_kg)
        val deliveredAvgWtElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_delivered_avg_wt)

        UIUtils.setUIElementValue(
            deliveredNumberElement,
            "$numberOfCustomersDelivered / $totalNumberOfCustomers Counters"
        )
        UIUtils.setUIElementValue(
            deliveredPcKgElement,
            "$deliveredPc pc - $deliveredKg kg"
        )
        UIUtils.setUIElementValue(
            deliveredAvgWtElement,
            "${WeightUtils.roundOff3places(avgWt)} kg/pc"
        )
    }

    fun onClickUpdateDashboard(view: View) {
        updateDashboard(false)
    }

    fun updateDashboard(useCache: Boolean) {
        SingleAttributedData.getRecords(useCache)
        DeliverToCustomerDataHandler.get(useCache)
        GetCustomerOrders.get(useCache)

        updateLoadInfo(useCache)
        updateDeliveredInfo(useCache)
        updateProjectedInfo(useCache)
    }

    private fun updateProjectedInfo(useCache: Boolean) {
        val projectedShortageElement = findViewById<TextView>(R.id.activity_admin_delivery_dashboard_projected_shortage)
        try {
            val deliveredPc = DeliverToCustomerCalculations.getTotalPcDelivered()
            val deliveredKg = DeliverToCustomerCalculations.getTotalKgDelivered()
            val deliveredAvgWt = deliveredKg / deliveredPc

            val metadataObj = SingleAttributedData.getRecords()
            val loadedAvgWt = NumberUtils.getDoubleOrZero(metadataObj.actualLoadKg) / NumberUtils.getDoubleOrZero(metadataObj.actualLoadPc)
            val shortage = (loadedAvgWt - deliveredAvgWt) * 100 / loadedAvgWt
            UIUtils.setUIElementValue(projectedShortageElement, "${WeightUtils.roundOff3places(shortage)}")
        } catch (e: Exception) {
            UIUtils.setUIElementValue(projectedShortageElement, "N/A")
        }
    }

    fun onClickSpoolCustomerData(view: View) {
        val spoolBtn = findViewById<Button>(R.id.admin_dashboard_spool_customer_delivery_data)
        Thread {
            runOnUiThread {
                spoolBtn.isEnabled = false
                spoolBtn.alpha = .5f
                spoolBtn.isClickable = false
                spoolBtn.text = "Finalizing Data... .. ."
            }
            CustomerData.spoolDeliveringData()
            Refueling.spoolRefuelingData()
            DaySummary.saveToServer()
            runOnUiThread {
                spoolBtn.isEnabled = true
                spoolBtn.alpha = 1.0f
                spoolBtn.isClickable = true
                spoolBtn.text = "Finalize Data"
            }
        }.start()
    }

    fun onClickSaveRate(view: View) {
        val obj = SingleAttributedData.getRecords()
        obj.load_companyName = UIUtils.getUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_load_company))
        obj.load_branch = UIUtils.getUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_load_company_branch))
        obj.load_account = UIUtils.getUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_load_account))
        obj.finalFarmRate = UIUtils.getUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_farmrate))
        obj.bufferRate = UIUtils.getUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_buffer_price))
        SingleAttributedData.save(obj)
    }

    fun setCompanyAndRateValuesInUI() {
        val obj = SingleAttributedData.getRecords()
        UIUtils.setUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_load_company), obj.load_companyName)
        UIUtils.setUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_load_company_branch), obj.load_branch)
        UIUtils.setUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_load_account), obj.load_account)
        UIUtils.setUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_farmrate), obj.finalFarmRate)
        UIUtils.setUIElementValue(findViewById<EditText>(R.id.activity_admin_delivery_dashboard_buffer_price), obj.bufferRate)
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}