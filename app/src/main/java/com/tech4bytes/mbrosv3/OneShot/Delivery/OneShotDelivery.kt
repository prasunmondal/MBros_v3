package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.clients.GScript
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessData.DayMetadata
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliveringUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.SMSOrderModelUtil
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.OneShot.RefuelUI
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import org.apache.commons.collections4.CollectionUtils

class OneShotDelivery : AppCompatActivity() {

    var deliverRecords: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
    lateinit var saveOneSortDeliveryButton: TextView
    lateinit var deleteDeliveryDataButton: TextView
    lateinit var sidebarIconLoadDetails: ImageView
    lateinit var sidebarIconDelivery: ImageView
    lateinit var sidebarIconRefuel: ImageView
    lateinit var sidebarIconOtherExpenses: ImageView
    lateinit var scrollview: ScrollView
    private lateinit var loadPcElement: EditText
    private lateinit var loadKgElement: EditText
    private lateinit var loadAvgWtElement: TextView
    private lateinit var refuelUIObj: RefuelUI

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this)
        AppUtils.logError(this)
        OSDDeliveryEntryInfo.setActivityContext(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        initializeVariables()
        val deliveryPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)

        Thread {
            populateDeliveryMap()
            OSDLoadInfo.updateSingleAttributedDataOnUI(this, loadPcElement, loadKgElement)
            refuelUIObj.initializeFinalKm()
            refuelUIObj.initiallizeRefuelUI()
            runOnUiThread {
                showOrders()
                OSDLoadInfo.updateLoadAvgWt(loadPcElement, loadKgElement, loadAvgWtElement)
                populateCustomerListDropdown()
                OSDLoadInfo.setListeners(this, loadPcElement, loadKgElement, loadAvgWtElement, deliveryPriceElement)
            }
        }.start()
    }

    @SuppressLint("NewApi")
    private fun populateCustomerListDropdown() {
        val allCustomers = ListUtils.getAllPossibleValuesList(CustomerKYC.fetchAll().execute(), CustomerKYCModel::nameEng).toList()
        val customersInUI = ListUtils.getAllPossibleValuesList(deliverRecords.values.toList(), DeliverToCustomerDataModel::name).toList()
        val listToShow = CollectionUtils.subtract(allCustomers, customersInUI).toList().sorted()

        val uiView = findViewById<AutoCompleteTextView>(R.id.osd_customer_picker)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.template_dropdown_entry, listToShow)
        uiView.setAdapter(adapter)
        uiView.threshold = 0
        uiView.setText("")
        uiView.setOnTouchListener { _, _ ->
            uiView.showDropDown()
            uiView.requestFocus()
            false
        }
        uiView.setOnItemClickListener { adapterView, view, i, l ->
            addNewCustomer(uiView.text.toString())
            populateCustomerListDropdown()
            uiView.setText("")
            uiView.hint = "+Customer"
        }
    }

    private fun initializeVariables() {
        refuelUIObj = RefuelUI(this, findViewById(R.id.osd_scroll_to_element_car_expenses), findViewById(R.id.osd_scroll_to_element_refuel))
        saveOneSortDeliveryButton = findViewById(R.id.osd_save_btn)
        deleteDeliveryDataButton = findViewById(R.id.osd_reset_btn)
        scrollview = findViewById(R.id.osd_scrollview)
        sidebarIconDelivery = findViewById(R.id.osd_sidebar_icon_delivery)
        sidebarIconLoadDetails = findViewById(R.id.osd_sidebar_icon_load_details)
        sidebarIconRefuel = findViewById(R.id.osd_sidebar_icon_refuel)
        sidebarIconOtherExpenses = findViewById(R.id.osd_sidebar_icon_other_expenses)
        loadPcElement = findViewById(R.id.one_shot_delivery_pc)
        loadKgElement = findViewById(R.id.one_shot_delivery_kg)
        loadAvgWtElement = findViewById(R.id.osd_loading_avg_wt)
    }

    private fun scrollToRow(scrollView: ScrollView, linearLayout: View, textViewToShow: View) {
        val delay: Long = 100 //delay to let finish with possible modifications to ScrollView
        scrollView.postDelayed({
            val textRect = Rect() //coordinates to scroll to
            textViewToShow.getHitRect(textRect) //fills textRect with coordinates of TextView relative to its parent (LinearLayout)
            scrollView.requestChildRectangleOnScreen(linearLayout, textRect, false) //ScrollView will make sure, the given textRect is visible
        }, delay)
    }

    @RequiresApi(34)
    private fun populateDeliveryMap() {
        deliverRecords = mutableMapOf()
        val listOfOrderedCustomers = SMSOrderModelUtil.fetchAll().execute()
        listOfOrderedCustomers.forEach {
            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = it.orderedPc.toString(),
                orderedKg = it.orderedKg.toString(),
                rate = "${CustomerDataUtils.getDeliveryRate(it.name)}",
                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
                customerAccount = it.name,
                deliveryStatus = "DELIVERING"
            )

            deliverRecords[it.name] = deliverCustomersOrders
        }

        val t = DeliveringUtils.fetchAll().execute()
        t.forEach {
            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = "0",
                orderedKg = "0",
                deliveredPc = it.deliveredPc,
                deliveredKg = it.deliveredKg,
                rate = "${CustomerDataUtils.getDeliveryRate(it.name)}",
                customerAccount = it.name,
                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
                deliveryStatus = "DELIVERING"
            )
            deliverRecords[it.name] = deliverCustomersOrders
        }
    }

    private fun addNewCustomer(name: String) {
        val key = addToUnOrderedMap(name)
        val uiFragment = OSDDeliveryEntryInfo.createOrderCard(this, key)
        OSDDeliveryEntryInfo.setListeners(this, key)
        findViewById<LinearLayout>(R.id.one_shot_delivery_unordered_customers_entry_container).addView(uiFragment)
        BalanceReferralCalculations.calculate(key)
    }

    private fun addToUnOrderedMap(name: String): DeliverToCustomerDataModel {
        val deliverCustomersOrders = DeliverToCustomerDataModel(
            id = "${System.currentTimeMillis()}",
            timestamp = DateUtils.getCurrentTimestamp(),
            name = name,
            orderedPc = "0",
            orderedKg = "0",
            rate = "${CustomerDataUtils.getDeliveryRate(name)}",
            prevDue = CustomerDueData.getLastFinalizedDue(name),
            deliveryStatus = "DELIVERING"
        )
        deliverRecords[name] = deliverCustomersOrders
        return deliverCustomersOrders
    }

    private fun showOrders() {
        var t = showOrders(deliverRecords, R.id.one_shot_delivery_ordered_customers_entry_container)
        findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).removeAllViews()

        t.forEach { (deliverObj, view) ->
            findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).addView(view)
            updateTotals()
        }
        updateTotals(this, false)
    }


    private fun showOrders(listOfCustomers: MutableMap<String, DeliverToCustomerDataModel>, container: Int): MutableMap<DeliverToCustomerDataModel, View> {
        val listContainer = findViewById<LinearLayout>(container)
        val entryMap: MutableMap<DeliverToCustomerDataModel, View> = mutableMapOf()

        listContainer.removeAllViews()

        listOfCustomers.forEach { order ->
            val entry = OSDDeliveryEntryInfo.createOrderCard(this, order.value)
            entryMap[order.value] = entry
        }

        listOfCustomers.forEach { order ->
            OSDDeliveryEntryInfo.setListeners(this, order.value)
        }
        return entryMap
    }

    companion object {
        fun updateTotals(context: OneShotDelivery = AppContexts.get() as OneShotDelivery, needsSave: Boolean = true) {
            val metadataObj = DayMetadata.getRecords()
            val totalPcElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_pc)
            val totalKgElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_kg)
            val totalSaleElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_sale)
            val totalShortageElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_shortage)
            val totalCollectedElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_collected_amount)
            val khataDue = context.findViewById<TextView>(R.id.osd_khata_due)
            val profitElement = context.findViewById<TextView>(R.id.osd_profit)
            val totalDueElement = context.findViewById<TextView>(R.id.osd_total_due)

            var sumPc = 0
            var sumKg = 0.0
            var sumSale = 0
            var sumAmountCollected = 0
            var sumKhataDue = 0
            var sumTotalDue = 0

            context.deliverRecords.forEach {
                val kg = NumberUtils.getDoubleOrZero(it.value.deliveredKg)
                if(kg > 0.0) {
                    sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
                }
                sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
                sumSale += NumberUtils.getIntOrZero(it.value.deliverAmount)
                sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
                if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                    sumKhataDue += NumberUtils.getIntOrZero(it.value.khataBalance)
                    sumTotalDue += NumberUtils.getIntOrZero(it.value.totalBalance)
                }
            }

            val sumCashPayments = DeliverToCustomerCalculations.getTotalAmountPaidInCashTodayByCustomers()
            val sumOnlinePayments = DeliverToCustomerCalculations.getTotalAmountPaidOnlineTodayByCustomers()
            val loadedKg = NumberUtils.getDoubleOrZero(DayMetadata.getRecords().actualLoadKg)
            val shortage = (loadedKg - sumKg) * 100 / loadedKg
            val loadedPc = NumberUtils.getIntOrZero(DayMetadata.getRecords().actualLoadPc)

            totalPcElement.text = "$sumPc"
            totalKgElement.text = "%.3f".format(sumKg)
            totalSaleElement.text = "$sumSale"
            totalShortageElement.text = "▼ ${"%.3f".format(shortage)} kg"
            totalCollectedElement.text = "$sumAmountCollected"
            totalCollectedElement.tooltipText="Cash:   $sumCashPayments\nOnline: $sumOnlinePayments "
            khataDue.text = "$sumKhataDue"
            profitElement.text = DaySummaryUtils.showDayProfit(sumSale)
            totalDueElement.text = "$sumTotalDue"

            if(sumPc != loadedPc) {
                totalPcElement.setBackgroundColor(ContextCompat.getColor(context, R.color.osd_total_bar_incorrect_data_background))
                totalPcElement.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                totalPcElement.setBackgroundColor(0x00000000)
                totalPcElement.setTextColor(ContextCompat.getColor(context, R.color.osd_total_bar_total_pc_correct_text_color))
            }

            if (needsSave)
                DayMetadata.saveToLocal(metadataObj)
        }
    }

    @SuppressLint("NewApi")
    fun onClickSaveOneShotDeliveryDataBtn(view: View) {
        Thread {
            runOnUiThread()
            {
                findViewById<ProgressBar>(R.id.osd_save_progress_bar).visibility = View.VISIBLE
                saveOneSortDeliveryButton.isEnabled = false
                saveOneSortDeliveryButton.alpha = .5f
                saveOneSortDeliveryButton.isClickable = false
            }

            gatherSingleAttributedData()

            DayMetadata.insert(DayMetadata.getRecords()).queue()
            saveDeliveryData()
            refuelUIObj.saveFuelData()
            DayMetadata.fetchAll().queue()
            DeliveringUtils.fetchAll().queue()
            GScript.execute()
            DayMetadata.clearLocalObj()
            runOnUiThread()
            {
                saveOneSortDeliveryButton.isEnabled = true
                saveOneSortDeliveryButton.alpha = 1.0f
                saveOneSortDeliveryButton.isClickable = true
                findViewById<ProgressBar>(R.id.osd_save_progress_bar).visibility = View.GONE
            }
        }.start()
    }

    private fun setSaveProgressBar(value: Int) {
        findViewById<ProgressBar>(R.id.osd_save_progress_bar)
            .setProgress(
                value,
                true
            )
    }

    private fun gatherSingleAttributedData() {
        val obj = DayMetadata.getRecords()
        val salaryPaid = refuelUIObj.getSalaryPaid(findViewById(R.id.osd_scroll_to_element_car_expenses)) - NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DRIVER_SALARY))
        obj.vehicle_finalKm = refuelUIObj.getFinalKm()
        obj.labour_expenses = salaryPaid.toString()
        obj.extra_expenses = refuelUIObj.getExtraExpenses(findViewById(R.id.osd_scroll_to_element_car_expenses)).toString()
        obj.actualLoadKg = loadKgElement.text.toString()
        obj.actualLoadPc = loadPcElement.text.toString()
        DayMetadata.saveToLocal(obj)
    }

    private fun saveDeliveryData() {
        var eachStep = 0

        val allDeliveredRecords: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
        allDeliveredRecords.putAll(deliverRecords)

        allDeliveredRecords.forEach { (s, deliveryObj) ->
            val referCalcObj = BalanceReferralCalculations.getTotalDiscountFor(deliveryObj.name)
            deliveryObj.adjustments = referCalcObj.transferAmount.toString()
            deliveryObj.totalBalance = (NumberUtils.getIntOrZero(deliveryObj.totalBalance) - referCalcObj.balanceOfReferered).toString()
            deliveryObj.notes = referCalcObj.message
        }

        val filteredListToSave = filterListToGetDataToSave(allDeliveredRecords)

        filteredListToSave.forEach {
            it.value.deliveryStatus = "DELIVERED"
        }
        runOnUiThread { setSaveProgressBar(eachStep) }
        DeliveringUtils.save(filteredListToSave.values.toList()).queue()
        runOnUiThread { setSaveProgressBar(100) }
    }

    private fun filterListToGetDataToSave(map: MutableMap<String, DeliverToCustomerDataModel>): Map<String, DeliverToCustomerDataModel> {
        return map.filter { x -> NumberUtils.getDoubleOrZero(x.value.deliveredKg) > 0.0
                || NumberUtils.getIntOrZero(x.value.paid) > 0
                || NumberUtils.getIntOrZero(x.value.adjustments) != 0 }
    }

    fun onClickToggleProfitViewUI(view: View) {
        val profitViewContainer = findViewById<LinearLayout>(R.id.osd_profit_details_container)
        profitViewContainer.visibility = if (profitViewContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
//        updateHiddenData()
    }

    override fun onBackPressed() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Unsaved changes will be lost. Do you want to go back?")
            .setTitle("Going back")
            .setPositiveButton("Yes") { dialog, id ->
                // CONFIRM
                val switchActivityIntent = Intent(this, ActivityLogin::class.java)
                switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(switchActivityIntent)
            }
            .setNegativeButton("No") { dialog, id ->
                // CANCEL
            }.setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun onClickGoToMoneyVerification(view: View) {
        val switchActivityIntent = Intent(this, CollectorVerifyMoneyCollectionActivity::class.java)
        startActivity(switchActivityIntent)
    }

    fun onClickDeleteDeliveryDataBtn(view: View) {
        Thread {
            runOnUiThread()
            {
                deleteDeliveryDataButton.isEnabled = false
                deleteDeliveryDataButton.alpha = 0.5f
                deleteDeliveryDataButton.isClickable = false
            }
            DeliveringUtils.deleteAll()
            runOnUiThread()
            {
                deleteDeliveryDataButton.isEnabled = true
                deleteDeliveryDataButton.alpha = 1.0f
                deleteDeliveryDataButton.isClickable = true
            }
        }.start()
    }

    fun onClickSendLoadInfoToCompany(view: View) {
        OSDLoadInfo.sendLoadInfoToCompany(loadPcElement.text.toString(), loadKgElement.text.toString())
    }
}