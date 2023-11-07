package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SheetCalculator
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrdersUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotDelivery
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.WeightUtils.WeightUtils
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.io.path.toPath


class ActivityAdminDeliveryDashboard : AppCompatActivity() {

    private lateinit var deliveredNumberElement: TextView
    private lateinit var totalPcElement: TextView
    private lateinit var totalKgElement: TextView
    private lateinit var avgWtElement: TextView
    private lateinit var deliveredPcKgElement: TextView
    private lateinit var deliveredAvgWtElement: TextView
    private lateinit var projectedShortageElement: TextView
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
    private lateinit var finalizingStatusIndicator: TextView
    private lateinit var resetStatusIndicator: TextView
    private var isFinalisedDone: Boolean? = null
    private var isResetDone: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_delivery_dashboard)
        supportActionBar!!.hide()
        AppContexts.set(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        AppUtils.logError()
        getPermissions()

        initializeVariables()
        updateDashboard(true)
        setCompanyAndRateValuesInUI()
        setRuntimeUIValues()
        setStatuses(false)
        setListeners()
    }

    private fun setStatuses(useCache: Boolean) {
        setSheetCalculatorCorrectness(false)
        setFinalizedIndicator(useCache)
        setResetIndicator(useCache)
    }

    private fun setSheetCalculatorCorrectness(useCache: Boolean) {
        Thread {
            val khataCorrectnessStatus = SheetCalculator.isKhataGreen(useCache)

            runOnUiThread {
                val khataCorrectnessIndicator = findViewById<TextView>(R.id.dashboard_check_khata_status)
                val khataCorrectnessText = if (khataCorrectnessStatus) "Verified" else "Incorrect"
                val khataCorrectnessColor = if (khataCorrectnessStatus) R.color.delivery_input_valid else R.color.delivery_input_not_valid
                khataCorrectnessIndicator.text = khataCorrectnessText
                khataCorrectnessIndicator.setTextColor(ContextCompat.getColor(this, khataCorrectnessColor))
            }
        }.start()
    }

    private fun isKhataGreen() {

    }

    private fun setFinalizedIndicator(useCache: Boolean) {
        Thread {
            if (isFinalised(useCache)) {
                isFinalisedDone = true
                runOnUiThread {
                    finalizingStatusIndicator.text = "Done"
                    finalizingStatusIndicator.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_valid))
                    finalizingStatusIndicator.setOnClickListener {}
                }
            } else {
                runOnUiThread {
                    finalizingStatusIndicator.text = "Pending (Click to Start)"
                    finalizingStatusIndicator.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
                    finalizingStatusIndicator.setOnClickListener {
                        finalizingStatusIndicator.text = "In Progress..."
                        finalizingStatusIndicator.setOnClickListener {}
                        spoolCustomerData()
                    }
                }
            }
        }.start()
    }

    private fun setResetIndicator(useCache: Boolean) {
        Thread {
            if (isResetDone(useCache)) {
                isResetDone = true
                runOnUiThread {
                    resetStatusIndicator.text = "Done"
                    resetStatusIndicator.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_valid))
                    resetStatusIndicator.setOnClickListener {}
                }
            } else {
                runOnUiThread {
                    resetStatusIndicator.text = "Pending (Click to Start)"
                    resetStatusIndicator.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
                    resetStatusIndicator.setOnClickListener {
                        if (isFinalised(useCache)) {
                            resetStatusIndicator.text = "In Progress..."
                            onClickDeleteDeliveryDataBtn()
                        } else {
                            confirmDailyRecordDeletion("WARNING", "Ideally data should be finalized before deleting the records. Do you want to proceed?")
                        }
                    }
                }
            }
        }.start()
    }

    private fun confirmDailyRecordDeletion(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setTitle(title)
            .setPositiveButton("Confirm") { dialog, id ->
                // CONFIRM
                resetStatusIndicator.text = "In Progress..."
                resetStatusIndicator.setOnClickListener {}
                onClickDeleteDeliveryDataBtn()
                isResetDone = true
            }
            .setNegativeButton("Cancel") { dialog, id ->
                // CANCEL
            }.setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun isFinalised(useCache: Boolean = true): Boolean {
        if (isFinalisedDone != null) {
            return isFinalisedDone!!
        }
        val bufferKm = NumberUtils.getIntOrZero(SingleAttributedData.getRecords(useCache).vehicle_finalKm)
        val lastFinalizedKm = DaySummary.getPrevTripEndKm(useCache)
        isFinalisedDone = (lastFinalizedKm == bufferKm || bufferKm == 0)
        return isFinalisedDone!!
    }

    private fun isResetDone(useCache: Boolean): Boolean {
        if (isResetDone != null) {
            return isResetDone!!
        }
        isResetDone = DeliverToCustomerDataHandler.get(useCache).isEmpty()
        return isResetDone!!
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

    private fun initializeVariables() {
        deliveredNumberElement = findViewById(R.id.activity_admin_delivery_dashboard_delivered_number)
        totalPcElement = findViewById(R.id.activity_admin_delivery_dashboard_total_pc)
        totalKgElement = findViewById(R.id.activity_admin_delivery_dashboard_loaded_kg)
        avgWtElement = findViewById(R.id.activity_admin_delivery_dashboard_total_loaded_avg_wt)
        deliveredPcKgElement = findViewById(R.id.activity_admin_delivery_dashboard_delivered_pc_kg)
        deliveredAvgWtElement = findViewById(R.id.activity_admin_delivery_dashboard_delivered_avg_wt)
        projectedShortageElement = findViewById(R.id.activity_admin_delivery_dashboard_projected_shortage)
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
        finalizingStatusIndicator = findViewById(R.id.dashboard_finalizing_status_indicator)
        resetStatusIndicator = findViewById(R.id.dashboard_reset_status_indicator)
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
        val totalNumberOfCustomers = GetCustomerOrdersUtils.getNumberOfCustomersOrdered(useCache)
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

    private fun spoolCustomerData() {
        Thread {
            val obj = SingleAttributedData.getRecords()
            obj.finalFarmRate = UIUtils.getUIElementValue(farmRateElement)
            val deliveryRate = UIUtils.getUIElementValue(deliveryRateElement)
            obj.bufferRate = DeliveryCalculations.getBufferPrice(obj.finalFarmRate, deliveryRate).toString()
            SingleAttributedData.save(obj)
            CustomerDataUtils.spoolDeliveringData()
            Refueling.spoolRefuelingData()
            DaySummary.saveToServer()
            SingleAttributedData.invalidateCache()
            setStatuses(false)
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

    fun onClickDeleteDeliveryDataBtn() {
        Thread {
            OneShotDelivery.deleteDeliveryDataOnServer()
            setStatuses(false)
        }.start()
    }

    fun onClickSendKhata(view: View) {
        Toast.makeText(this, "Downloading Daily File", Toast.LENGTH_SHORT).show()
        val filename = "MBros - ${DateUtils.getDateInFormat(Date(), "yyyy.MM.dd")}"
        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI().toPath().toString() + "/" + filename + ".pdf"

        Thread {
            downloadDailySheet(filePath)
            runOnUiThread {
                Toast.makeText(this, "Download Complete", Toast.LENGTH_SHORT).show()
            }
            Whatsapp.sendFileUsingWhatsapp(this, filePath, "919679004046", "")
        }.start()
    }

    fun downloadDailySheet(fullPath: String) {
        try {
            val u = URL("https://docs.google.com/spreadsheets/d/e/2PACX-1vQSO3BWQ7b0JmySpKVSULco9FcxrDi3UX9uSIECvOdUCSUI8AyeCDjSnmwWeA-l6oHBkUNhDjTU7Rgd/pub?gid=1385397548&single=true&output=pdf")
            val iStream: InputStream = u.openStream()
            val dis = DataInputStream(iStream)
            val buffer = ByteArray(1024)
            var length: Int
            val fos = FileOutputStream(File(fullPath))
            while (dis.read(buffer).also { length = it } > 0) {
                fos.write(buffer, 0, length)
            }
        } catch (mue: MalformedURLException) {
            Log.e("SYNC getUpdate", "malformed url error", mue)
        } catch (ioe: IOException) {
            Log.e("SYNC getUpdate", "io error", ioe)
        } catch (se: SecurityException) {
            Log.e("SYNC getUpdate", "security error", se)
        }
    }
}