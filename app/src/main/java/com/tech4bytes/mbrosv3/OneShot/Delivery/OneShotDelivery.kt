package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataModel
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerConfig
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrderUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.OneShot.RefuelUI
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Android.MeteredNumbers
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.VehicleManagement.RefuelingUtils
import org.apache.commons.collections4.CollectionUtils

class OneShotDelivery : AppCompatActivity() {

    var deliverRecords: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
    lateinit var saveOneSortDeliveryButton: Button
    lateinit var deleteDeliveryDataButton: Button
    lateinit var sidebarIconLoadDetails: ImageView
    lateinit var sidebarIconDelivery: ImageView
    lateinit var sidebarIconRefuel: ImageView
    lateinit var sidebarIconOtherExpenses: ImageView
    lateinit var refuelContainer: LinearLayout
    lateinit var scrollview: ScrollView
    private lateinit var extraExpensesElement: EditText
    private lateinit var loadPcElement: EditText
    private lateinit var loadKgElement: EditText
    private lateinit var loadAvgWtElement: TextView


//    private lateinit var refuelQtyElement: EditText
//    private lateinit var refuelAmountElement: EditText

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
            initiallizeUI()
            RefuelUI.initializeFinalKm(this, findViewById(R.id.osd_scroll_to_element_car_expenses))
            RefuelUI.initiallizeRefuelUI(this, findViewById(R.id.osd_scroll_to_element_refuel))
            runOnUiThread {
                showOrders()
                OSDLoadInfo.updateRelatedFields_LoadPcKg(loadPcElement, loadKgElement, loadAvgWtElement)
                populateCustomerListDropdown()
                OSDLoadInfo.setListeners(this, loadPcElement, loadKgElement, loadAvgWtElement, deliveryPriceElement)
            }
        }.start()
    }

    @SuppressLint("NewApi")
    private fun populateCustomerListDropdown() {
        val allCustomers = ListUtils.getAllPossibleValuesList(CustomerKYC.get(), CustomerKYCModel::nameEng).toList()
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
        saveOneSortDeliveryButton = findViewById(R.id.one_shot_delivery_save_data_btn)
        deleteDeliveryDataButton = findViewById(R.id.osd_delete_delivery_data)
        scrollview = findViewById(R.id.osd_scrollview)
        sidebarIconDelivery = findViewById(R.id.osd_sidebar_icon_delivery)
        sidebarIconLoadDetails = findViewById(R.id.osd_sidebar_icon_load_details)
        sidebarIconRefuel = findViewById(R.id.osd_sidebar_icon_refuel)
        sidebarIconOtherExpenses = findViewById(R.id.osd_sidebar_icon_other_expenses)
        refuelContainer = findViewById(R.id.osd_refuel_container)
        loadPcElement = findViewById(R.id.one_shot_delivery_pc)
        loadKgElement = findViewById(R.id.one_shot_delivery_kg)
        loadAvgWtElement = findViewById(R.id.osd_loading_avg_wt)
    }

    fun onClickSidebarIconRefuel(view: View) {
        scrollToRow(scrollview, findViewById(R.id.osd_scrollview_child), findViewById<LinearLayout>(R.id.osd_scroll_to_element_refuel))
    }

    fun onClickSidebarIconDeliveryEntries(view: View) {
        scrollToRow(scrollview, findViewById(R.id.osd_scrollview_child), findViewById<LinearLayout>(R.id.osd_scroll_label_deliveries))
    }

    fun onClickSidebarIconLoadInfo(view: View) {
        scrollToRow(scrollview, findViewById(R.id.osd_scrollview_child), findViewById<LinearLayout>(R.id.osd_scroll_label_load_info))
    }

    fun onClickSidebarIconOtherExpenses(view: View) {
        scrollToRow(scrollview, findViewById(R.id.osd_scrollview_child), findViewById<LinearLayout>(R.id.osd_scroll_to_element_other_expenses))
    }

    private fun scrollToRow(scrollView: ScrollView, linearLayout: View, textViewToShow: View) {
        val delay: Long = 100 //delay to let finish with possible modifications to ScrollView
        scrollView.postDelayed({
            val textRect = Rect() //coordinates to scroll to
            textViewToShow.getHitRect(textRect) //fills textRect with coordinates of TextView relative to its parent (LinearLayout)
            scrollView.requestChildRectangleOnScreen(linearLayout, textRect, false) //ScrollView will make sure, the given textRect is visible
        }, delay)
    }

    private fun initiallizeUI() {


        OSDLoadInfo.initializeUI(this, loadPcElement, loadKgElement, loadAvgWtElement)
    }

//    private fun initializeOtherExpensesUI() {
//
//    }

    @RequiresApi(34)
    private fun populateDeliveryMap() {
        deliverRecords = mutableMapOf()
        val listOfOrderedCustomers = GetCustomerOrderUtils.getListOfOrderedCustomers()
        listOfOrderedCustomers.forEach {
            var customerAccount = CustomerKYC.getByName(it.name)!!.referredBy
            if (customerAccount.isEmpty())
                customerAccount = it.name

            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = it.orderedPc,
                orderedKg = it.orderedKg,
                rate = "${CustomerDataUtils.getDeliveryRate(it.name)}",
                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
                customerAccount = it.name,
                deliveryStatus = "DELIVERING"
            )

            deliverRecords[it.name] = deliverCustomersOrders
        }

        val t = DeliverToCustomerDataHandler.get()
        t.forEach {
            var customerAccount = CustomerKYC.getByName(it.name)!!.referredBy
            if (customerAccount.isEmpty())
                customerAccount = it.name
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

//        deliveryMapUnOrderedCustomers = mutableMapOf()
//        val listOfUnOrderedCustomers = GetCustomerOrderUtils.getListOfUnOrderedCustomers()
//        listOfUnOrderedCustomers.forEach {
//            var customerAccount = CustomerKYC.getByName(it.name)!!.referredBy
//            if (customerAccount.isEmpty())
//                customerAccount = it.name
//            val deliverCustomersOrders = DeliverToCustomerDataModel(
//                id = "${System.currentTimeMillis()}",
//                timestamp = DateUtils.getCurrentTimestamp(),
//                name = it.name,
//                orderedPc = "0",
//                orderedKg = "0",
//                rate = "${CustomerDataUtils.getDeliveryRate(it.name)}",
//                customerAccount = customerAccount,
//                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
//                deliveryStatus = "DELIVERING"
//            )
//
//            deliveryMapOrderedCustomers[it.name] = deliverCustomersOrders
//        }
    }

    private fun addNewCustomer(name: String) {
        val key = addToUnOrderedMap(name)
        val uiFragment = OSDDeliveryEntryInfo.createOrderCard(this, key)
        OSDDeliveryEntryInfo.setListeners(this, key)
        showOrder(key, uiFragment)
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

    private fun showOrder(key: DeliverToCustomerDataModel, value: View) {
        OSDDeliveryEntryInfo.updateEntry(this, key, value)
        OSDDeliveryEntryInfo.updateAvgKg(value)
        findViewById<LinearLayout>(R.id.one_shot_delivery_unordered_customers_entry_container).addView(value)
    }

    private fun showOrders() {
        var t = showOrders(deliverRecords, R.id.one_shot_delivery_ordered_customers_entry_container)
        findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).removeAllViews()

        t.forEach { (key, value) ->
            OSDDeliveryEntryInfo.updateEntry(this, key, value, false)
            OSDDeliveryEntryInfo.updateAvgKg(value)
            findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).addView(value)
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

    private fun updateHiddenData() {
        val profitViewContainer = findViewById<LinearLayout>(R.id.osd_profit_details_container)
        if (profitViewContainer.visibility == View.VISIBLE) {
            val profitElement = findViewById<TextView>(R.id.osd_profit)
            val totalDueElement = findViewById<TextView>(R.id.osd_total_due)

            profitElement.text = DaySummaryUtils.showDayProfit()
            totalDueElement.text = "---"
        }
        updateTotalDueBalance()
    }

    fun updateTotalDueBalance() {
        var sum = 0
        CustomerDueData.getBalance().forEach { (s, i) -> sum += i }
        val totalDueElement = findViewById<TextView>(R.id.osd_total_due)
        runOnUiThread {
            totalDueElement.text = NumberUtils.getIntOrZero(sum.toString()).toString()
        }
    }

    companion object {
        fun updateTotals(context: OneShotDelivery, needsSave: Boolean = true) {
            val metadataObj = SingleAttributedDataUtils.getRecords()
            val totalPcElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_pc)
            val totalKgElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_kg)
            val totalSaleElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_sale)
            val totalShortageElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_shortage)
            val totalCollectedElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_collected_amount)
            val totalBalanceDueElement = context.findViewById<TextView>(R.id.one_shot_delivery_total_balance_due)

            var sumPc = 0
            var sumKg = 0.0
            var sumSale = 0
            var sumAmountCollected = 0
            var sumBalanceDue = 0

            context.deliverRecords.forEach {
                sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
                sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
                sumSale += NumberUtils.getIntOrZero(it.value.todaysAmount)
                sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
                if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                    sumBalanceDue += NumberUtils.getIntOrZero(it.value.balanceDue)
                }
            }

            val loadedKg = NumberUtils.getDoubleOrZero(SingleAttributedDataUtils.getRecords().actualLoadKg)
            val shortage = (loadedKg - sumKg) * 100 / loadedKg

            val loadedPc = NumberUtils.getIntOrZero(SingleAttributedDataUtils.getRecords().actualLoadPc)
            totalPcElement.text = "$sumPc"
            if(sumPc != loadedPc) {
                totalPcElement.setBackgroundColor(ContextCompat.getColor(context, R.color.osd_total_bar_incorrect_data_background))
                totalPcElement.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                totalPcElement.setBackgroundColor(0x00000000)
                totalPcElement.setTextColor(ContextCompat.getColor(context, R.color.osd_total_bar_total_pc_correct_text_color))
            }

            totalKgElement.text = "%.3f".format(sumKg)
            totalSaleElement.text = "$sumSale"
            metadataObj.daySale = sumSale.toString()

            totalShortageElement.text = "▼ ${"%.3f".format(shortage)} kg"
            totalCollectedElement.text = "$sumAmountCollected"
            totalBalanceDueElement.text = "$sumBalanceDue"

            if (needsSave)
                SingleAttributedDataUtils.saveToLocal(metadataObj)

            context.updateHiddenData()
        }

        fun deleteDeliveryDataOnServer() {
            Delete.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
                .build().execute()
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
            RefuelUI.gatherFuelData(findViewById(R.id.osd_scroll_to_element_refuel))
            DeliverToCustomerDataHandler.deleteData()

            saveSingleAttributeData()
            saveDeliveryData()
            SingleAttributedDataUtils.getRecords()
            DeliverToCustomerDataHandler.get()
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
        val obj = SingleAttributedDataUtils.getRecords()
        val salaryPaid = RefuelUI.getSalaryPaid(findViewById(R.id.osd_scroll_to_element_car_expenses)) - NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DRIVER_SALARY))
        obj.vehicle_finalKm = RefuelUI.getFinalKm()
        obj.labour_expenses = salaryPaid.toString()
        obj.extra_expenses = NumberUtils.getIntOrZero(extraExpensesElement.text.toString()).toString()
        obj.actualLoadKg = loadKgElement.text.toString()
        obj.actualLoadPc = loadPcElement.text.toString()

        SingleAttributedDataUtils.saveToLocal(obj)
    }

    private fun saveSingleAttributeData() {
        Thread {
            SingleAttributedDataUtils.saveToLocalThenServer(SingleAttributedDataUtils.getRecords())
            setSaveProgressBar(10)
        }.start()
    }

    private fun saveDeliveryData() {
        var eachStep = 0

        val allDeliveredRecords: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
        allDeliveredRecords.putAll(deliverRecords)

        allDeliveredRecords.forEach { (s, deliveryObj) ->
            val referCalcObj = BalanceReferralCalculations.getTotalDiscountFor(deliveryObj.name)
            deliveryObj.adjustments = referCalcObj.transferAmount.toString()
            deliveryObj.balanceDue = (NumberUtils.getIntOrZero(deliveryObj.balanceDue) - referCalcObj.balanceOfReferered).toString()
            deliveryObj.notes = referCalcObj.message
        }

        val filteredListToSave = filterListToGetDataToSave(allDeliveredRecords)

        // save locally
        filteredListToSave.forEach {
            if(it.value.date.isEmpty()) {
                it.value.date = DateUtils.getDateInFormat("dd/MM/yyyy")

                it.value.customerAccount = it.value.name
//                if(CustomerKYC.getByName(it.value.name) == null || CustomerKYC.getByName(it.value.name)!!.referredBy.isEmpty()) {
//                    it.value.name
//                } else {
//                    CustomerKYC.getByName(it.value.name)!!.referredBy
//                }
            }
            DeliverToCustomerDataHandler.saveToLocal(it.value)
        }

        // save to server
        filteredListToSave.forEach {
            it.value.deliveryStatus = "DELIVERED"
            DeliverToCustomerDataHandler.saveToServer(it.value)
            if (eachStep + 10 < 100) {
                eachStep += 10
            } else {
                eachStep = 100
            }
            runOnUiThread { setSaveProgressBar(eachStep) }
        }
        runOnUiThread { setSaveProgressBar(100) }
    }

    private fun filterListToGetDataToSave(map: MutableMap<String, DeliverToCustomerDataModel>): Map<String, DeliverToCustomerDataModel> {
        return map.filter { x -> shouldRecordThisTransaction(x) }
    }

    private fun shouldRecordThisTransaction(it: Map.Entry<String, DeliverToCustomerDataModel>): Boolean {
        return NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0
                || NumberUtils.getIntOrZero(it.value.paid) > 0
                || NumberUtils.getIntOrZero(it.value.adjustments) != 0
    }

    fun onClickToggleProfitViewUI(view: View) {
        val profitViewContainer = findViewById<LinearLayout>(R.id.osd_profit_details_container)
        profitViewContainer.visibility = if (profitViewContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        updateHiddenData()
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
            deleteDeliveryDataOnServer()
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