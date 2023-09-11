package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.WeightUtils.WeightUtils
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling

class ActivityAdminDeliveryDashboard : AppCompatActivity() {

    private lateinit var deliveredNumberElement: TextView
    private lateinit var totalPcElement: TextView
    private lateinit var totalKgElement: TextView
    private lateinit var avgWtElement: TextView
    private lateinit var deliveredPcKgElement: TextView
    private lateinit var deliveredAvgWtElement: TextView
    private lateinit var projectedShortageElement: TextView
    private lateinit var spoolBtnElement: Button
    private lateinit var farmRateElement: EditText
    private lateinit var deliveryRateElement: EditText
    private lateinit var profitElement: TextView
    private lateinit var carCostElement: TextView
    private lateinit var labCostElement: TextView
    private lateinit var extraCostElement: TextView
    private lateinit var loadCompanyElement: TextView
    private lateinit var loadBranchElement: TextView
    private lateinit var loadAccountElement: TextView
    private lateinit var loadAreaElement: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_delivery_dashboard)
        supportActionBar!!.hide()
        AppContexts.set(this)
        AppUtils.logError()
        getPermissions()

        initiallizeVariables()
        updateDashboard(true)
        setCompanyAndRateValuesInUI()
        setRuntimeUIValues()
        setListeners()
    }

    private fun setListeners() {
        farmRateElement.addTextChangedListener {
            val obj = SingleAttributedData.getRecords()
            obj.finalFarmRate = it.toString()
            SingleAttributedData.saveToLocal(obj)
            setRuntimeUIValues()
        }
        deliveryRateElement.addTextChangedListener {
            val obj = SingleAttributedData.getRecords()
            obj.bufferRate = DeliveryCalculations.getBufferPrice(obj.finalFarmRate, it.toString()).toString()
            SingleAttributedData.saveToLocal(obj)
            setRuntimeUIValues()
        }
    }

    private fun initiallizeVariables() {
        deliveredNumberElement = findViewById(R.id.activity_admin_delivery_dashboard_delivered_number)
        totalPcElement = findViewById(R.id.activity_admin_delivery_dashboard_total_pc)
        totalKgElement = findViewById(R.id.activity_admin_delivery_dashboard_loaded_kg)
        avgWtElement = findViewById(R.id.activity_admin_delivery_dashboard_total_loaded_avg_wt)
        deliveredPcKgElement = findViewById(R.id.activity_admin_delivery_dashboard_delivered_pc_kg)
        deliveredAvgWtElement = findViewById(R.id.activity_admin_delivery_dashboard_delivered_avg_wt)
        projectedShortageElement = findViewById(R.id.activity_admin_delivery_dashboard_projected_shortage)
        spoolBtnElement = findViewById(R.id.admin_dashboard_spool_customer_delivery_data)
        farmRateElement = findViewById(R.id.activity_admin_delivery_dashboard_farmrate)
        deliveryRateElement = findViewById(R.id.activity_admin_delivery_dashboard_delivery_base_price)
        profitElement = findViewById(R.id.admin_dash_profit)
        carCostElement = findViewById(R.id.admin_dash_car_cost)
        labCostElement = findViewById(R.id.admin_dash_lab_cost)
        extraCostElement = findViewById(R.id.admin_dash_extra_cost)
        loadCompanyElement = findViewById(R.id.activity_admin_delivery_dashboard_load_company)
        loadBranchElement = findViewById(R.id.activity_admin_delivery_dashboard_load_company_branch)
        loadAccountElement = findViewById(R.id.activity_admin_delivery_dashboard_load_account)
        loadAreaElement = findViewById(R.id.activity_admin_delivery_dashboard_load_company_area)
    }

    private fun getPermissions() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        val REQUEST_READ_PHONE_STATE = 1
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_READ_PHONE_STATE)
        }
    }

    fun updateLoadInfo(useCache: Boolean) {

        val loadMetadata = SingleAttributedData.getRecords(useCache)
        UIUtils.setUIElementValue(totalPcElement, loadMetadata.actualLoadPc)
        UIUtils.setUIElementValue(totalKgElement, "%.3f".format(WeightUtils.roundOff3places(loadMetadata.actualLoadKg)))

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

        UIUtils.setUIElementValue(
            deliveredNumberElement,
            "$numberOfCustomersDelivered / $totalNumberOfCustomers Counters"
        )
        UIUtils.setUIElementValue(
            deliveredPcKgElement,
            "$deliveredPc pc - ${"%.3f".format(WeightUtils.roundOff3places(deliveredKg))} kg"
        )
        UIUtils.setUIElementValue(
            deliveredAvgWtElement,
            "${WeightUtils.roundOff3places(avgWt)} kg/pc"
        )
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
        try {
            val metadataObj = SingleAttributedData.getRecords(useCache)
            val shortage = DeliveryCalculations.getShortage(NumberUtils.getDoubleOrZero(metadataObj.actualLoadKg), DeliverToCustomerCalculations.getTotalKgDelivered())
            UIUtils.setUIElementValue(projectedShortageElement, "${WeightUtils.roundOff3places(shortage)}")
        } catch (e: Exception) {
            UIUtils.setUIElementValue(projectedShortageElement, "N/A")
        }
    }

    fun onClickSpoolCustomerData(view: View) {

        Thread {
            runOnUiThread {
                spoolBtnElement.isEnabled = false
                spoolBtnElement.alpha = .5f
                spoolBtnElement.isClickable = false
                spoolBtnElement.text = "Finalizing Data... .. ."
            }
            val obj = SingleAttributedData.getRecords()
            obj.finalFarmRate = UIUtils.getUIElementValue(farmRateElement)
            val deliveryRate = UIUtils.getUIElementValue(deliveryRateElement)
            obj.bufferRate = DeliveryCalculations.getBufferPrice(obj.finalFarmRate, deliveryRate).toString()
            SingleAttributedData.save(obj)
            CustomerData.spoolDeliveringData()
            Refueling.spoolRefuelingData()
            DaySummary.saveToServer()
            SingleAttributedData.invalidateCache()
            runOnUiThread {
                spoolBtnElement.isEnabled = true
                spoolBtnElement.alpha = 1.0f
                spoolBtnElement.isClickable = true
                spoolBtnElement.text = "Finalize Data"
            }
        }.start()
    }

    fun setCompanyAndRateValuesInUI() {
        val obj = SingleAttributedData.getRecords()
        UIUtils.setUIElementValue(loadCompanyElement, obj.load_companyName)
        UIUtils.setUIElementValue(loadBranchElement, obj.load_branch)
        UIUtils.setUIElementValue(loadAccountElement, obj.load_account)
        UIUtils.setUIElementValue(loadAreaElement, obj.load_area)
        UIUtils.setUIElementValue(farmRateElement, obj.finalFarmRate)
        UIUtils.setUIElementValue(deliveryRateElement, DeliveryCalculations.getBaseDeliveryPrice(obj.finalFarmRate, obj.bufferRate).toString())
        UIUtils.setUIElementValue(carCostElement, DeliveryCalculations.getKmCost().toString())
        UIUtils.setUIElementValue(labCostElement, obj.labour_expenses)
        UIUtils.setUIElementValue(extraCostElement, obj.extra_expenses)
    }

    fun setRuntimeUIValues() {
        LogMe.log("Setting profit element to: ${DaySummary.showDayProfit()}")
        UIUtils.setUIElementValue(profitElement, DaySummary.showDayProfit())
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}