package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerConfig
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.VehicleManagement.RefuelingUtils
import org.apache.commons.collections4.CollectionUtils

class OneShotDelivery : AppCompatActivity() {

    var deliveryMapOrderedCustomers: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
    var deliveryMapUnOrderedCustomers: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
    lateinit var saveOneSortDeliveryButton: Button
    lateinit var deleteDeliveryDataButton: Button
    lateinit var sidebarIconLoadDetails: ImageView
    lateinit var sidebarIconDelivery: ImageView
    lateinit var sidebarIconRefuel: ImageView
    lateinit var sidebarIconOtherExpenses: ImageView
    lateinit var refuelContainer: LinearLayout
    lateinit var scrollview: ScrollView
    private lateinit var finalKmElement: EditText
    private lateinit var labourExpensesElement: EditText
    private lateinit var extraExpensesElement: EditText
    private lateinit var loadPcElement: EditText
    private lateinit var loadKgElement: EditText
    private lateinit var loadAvgWtElement: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this)
        AppUtils.logError()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        initializeVariables()
        val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
        val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)


        getSMSPermission()
        Thread {
            populateDeliveryMap()
            OSDLoadInfo.updateSingleAttributedDataOnUI(this, loadPcElement, loadKgElement)
            initiallizeUI()
            runOnUiThread {
                showOrders()
                initializeOtherExpensesUI()
                OSDLoadInfo.updateRelatedFields_LoadPcKg(loadPcElement, loadKgElement, loadAvgWtElement)
                initiallizeRefuelUI()
                updateKmRelatedCosts()
                populateCustomerListDropdown()
                OSDLoadInfo.setListeners(this, loadPcElement, loadKgElement, loadAvgWtElement, loadPriceElement, loadBufferElement)
            }
        }.start()
    }

    private fun populateCustomerListDropdown() {
        val allCustomers = ListUtils.getAllPossibleValuesList(CustomerKYC.getAllCustomers(), CustomerKYCModel::nameEng).toList()
        val customersInUI = ListUtils.getAllPossibleValuesList(deliveryMapOrderedCustomers.values.toList(), DeliverToCustomerDataModel::name).toList()
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
        finalKmElement = findViewById(R.id.one_shot_delivery_trip_end_km)
        labourExpensesElement = findViewById(R.id.one_shot_delivery_labour_expenses)
        extraExpensesElement = findViewById(R.id.one_shot_delivery_extra_expenses)
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
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)

        OSDLoadInfo.initializeUI(this, loadPcElement, loadKgElement, loadAvgWtElement)
        didRefuelElement.setOnCheckedChangeListener { _, isChecked ->
            val obj = SingleAttributedData.getRecords()
            obj.did_refueled = isChecked.toString()
            SingleAttributedData.saveToLocal(obj)
            updateRefuelingUIDetails()
        }

        didTankFullElement.setOnCheckedChangeListener { _, isChecked ->
            SingleAttributedData.saveAttributeToLocal(SingleAttributedData::refueling_isFullTank, isChecked.toString())
            updateRefuelingUIDetails()
        }

        refuelingKmElement.doOnTextChanged { text, start, before, count ->
            updateRefuelingUIDetails()
        }

        refuelingQtyElement.doOnTextChanged { text, start, before, count ->
            updateRefuelingUIDetails()
        }

        val record = SingleAttributedData.getRecords()

    }


    private fun initializeOtherExpensesUI() {
        val salaryDivisionElement = findViewById<TextView>(R.id.osd_salary_division)

        UIUtils.setUIElementValue(finalKmElement, SingleAttributedData.getRecords().vehicle_finalKm)
        UIUtils.setUIElementValue(labourExpensesElement, SingleAttributedData.getRecords().labour_expenses)
        UIUtils.setUIElementValue(extraExpensesElement, SingleAttributedData.getRecords().extra_expenses)
        UIUtils.setUIElementValue(salaryDivisionElement, SingleAttributedData.getRecords().salaryDivision.replace("#", "  #  "))

        finalKmElement.doOnTextChanged { text, start, before, count ->
            updateKmRelatedCosts()
        }

    }

    private fun updateKmRelatedCosts() {
        val currentKmOnUI = finalKmElement.text.toString()
        val prevKmElement = findViewById<TextView>(R.id.osd_prev_km)
        val kmDiffElement = findViewById<TextView>(R.id.osd_km_diff)
        val kmCostElement = findViewById<TextView>(R.id.osd_km_cost)

        val currentKm = NumberUtils.getIntOrZero(currentKmOnUI)
        val prevKm = DaySummary.getPrevTripEndKm()
        prevKmElement.text = prevKm.toString()

        if (currentKm < prevKm) {
            kmDiffElement.text = "N/A"
            kmCostElement.text = "N/A"
            return
        }

        val kmDiff = DeliveryCalculations.getKmDiff(currentKmOnUI)
        val kmCost = DeliveryCalculations.getKmCost(currentKmOnUI)

        val singleDataObj = SingleAttributedData.getRecords()
        singleDataObj.vehicle_finalKm = currentKm.toString()
        SingleAttributedData.saveToLocal(singleDataObj)

        kmDiffElement.text = kmDiff.toString()
        kmCostElement.text = kmCost.toString()
    }

    private fun initiallizeRefuelUI() {
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)
        val refuelingAmountElement = findViewById<EditText>(R.id.osd_refuel_amount)

        UIUtils.setUIElementValue(didRefuelElement, SingleAttributedData.getRecords().did_refueled)
        UIUtils.setUIElementValue(didTankFullElement, SingleAttributedData.getRecords().refueling_isFullTank)
        UIUtils.setUIElementValue(refuelingQtyElement, SingleAttributedData.getRecords().refueling_qty)
        UIUtils.setUIElementValue(refuelingKmElement, SingleAttributedData.getRecords().refueling_km)
        UIUtils.setUIElementValue(refuelingAmountElement, SingleAttributedData.getRecords().refueling_amount)

        refuelingKmElement.doOnTextChanged { text, start, before, count ->
            val refuelingKm = NumberUtils.getIntOrZero(refuelingKmElement.text.toString())
            val addKmToFuelKmToGetFinalKm = NumberUtils.getIntOrZero(AppConstants.get(AppConstants.ADD_TO_FUELING_KMS_TO_GET_FINAL_KM))
            finalKmElement.setText((refuelingKm + addKmToFuelKmToGetFinalKm).toString())
        }

        updateRefuelingUIDetails()
    }

    private fun populateDeliveryMap() {
        deliveryMapOrderedCustomers = mutableMapOf()
        val listOfOrderedCustomers = GetCustomerOrders.getListOfOrderedCustomers()
        listOfOrderedCustomers.forEach {
            var customerAccount = CustomerKYC.get(it.name)!!.customerAccount
            if(customerAccount.isEmpty())
                customerAccount = it.name

            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = it.orderedPc,
                orderedKg = it.orderedKg,
                rate = "${CustomerData.getDeliveryRate(it.name)}",
                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
                customerAccount = customerAccount,
                deliveryStatus = "DELIVERING"
            )

            deliveryMapOrderedCustomers[it.name] = deliverCustomersOrders
        }

        DeliverToCustomerDataHandler.get<DeliverToCustomerDataModel>().forEach {
            var customerAccount = CustomerKYC.get(it.name)!!.customerAccount
            if(customerAccount.isEmpty())
                customerAccount = it.name
            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = "0",
                orderedKg = "0",
                rate = "${CustomerData.getDeliveryRate(it.name)}",
                customerAccount = customerAccount,
                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
                deliveryStatus = "DELIVERING"
            )
            deliveryMapOrderedCustomers[it.name] = deliverCustomersOrders
        }

        deliveryMapUnOrderedCustomers = mutableMapOf()
        val listOfUnOrderedCustomers = GetCustomerOrders.getListOfUnOrderedCustomers()
        listOfUnOrderedCustomers.forEach {
            var customerAccount = CustomerKYC.get(it.name)!!.customerAccount
            if(customerAccount.isEmpty())
                customerAccount = it.name
            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = "0",
                orderedKg = "0",
                rate = "${CustomerData.getDeliveryRate(it.name)}",
                customerAccount = customerAccount,
                prevDue = CustomerDueData.getLastFinalizedDue(it.name),
                deliveryStatus = "DELIVERING"
            )

            deliveryMapUnOrderedCustomers[it.name] = deliverCustomersOrders
        }
    }

    private fun addNewCustomer(name: String) {
        val key = addToUnOrderedMap(name)
        val uiFragment = OSDDeliveryEntryInfo.createOrderCard(this, key)
        OSDDeliveryEntryInfo.setListeners(this, key)
        showOrder(key, uiFragment)
    }

    private fun addToUnOrderedMap(name: String): DeliverToCustomerDataModel {
        val deliverCustomersOrders = DeliverToCustomerDataModel(
            id = "${System.currentTimeMillis()}",
            timestamp = DateUtils.getCurrentTimestamp(),
            name = name,
            orderedPc = "0",
            orderedKg = "0",
            rate = "${CustomerData.getDeliveryRate(name)}",
            prevDue = CustomerDueData.getLastFinalizedDue(name),
            deliveryStatus = "DELIVERING"
        )
        deliveryMapUnOrderedCustomers[name] = deliverCustomersOrders
        return deliverCustomersOrders
    }

    private fun showOrder(key: DeliverToCustomerDataModel, value: View) {
        OSDDeliveryEntryInfo.updateEntry(this, key, value)
        findViewById<LinearLayout>(R.id.one_shot_delivery_unordered_customers_entry_container).addView(value)
    }

    private fun showOrders() {
        var t = showOrders(deliveryMapOrderedCustomers, R.id.one_shot_delivery_ordered_customers_entry_container)
        findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).removeAllViews()

        t.forEach { (key, value) ->
            OSDDeliveryEntryInfo.updateEntry(this, key, value, false)
            findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).addView(value)
        }
        updateTotals(this)
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

    private fun getSMSPermission() {
        val PERMISSION_REQUEST_CODE = 123
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            Log.d("permission", "permission denied to SEND_SMS - requesting it")
            val permissions = arrayOf(android.Manifest.permission.SEND_SMS)
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun updateHiddenData() {
        val profitViewContainer = findViewById<LinearLayout>(R.id.osd_profit_details_container)
        if (profitViewContainer.visibility == View.VISIBLE) {
            val profitElement = findViewById<TextView>(R.id.osd_profit)
            val totalDueElement = findViewById<TextView>(R.id.osd_total_due)

            profitElement.text = DaySummary.showDayProfit()
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
        fun updateTotals(context: OneShotDelivery) {
            val metadataObj = SingleAttributedData.getRecords()
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

            context.deliveryMapOrderedCustomers.forEach {
                sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
                sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
                sumSale += NumberUtils.getIntOrZero(it.value.todaysAmount)
                sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
                if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                    sumBalanceDue += NumberUtils.getIntOrZero(it.value.balanceDue)
                }
            }

            context.deliveryMapUnOrderedCustomers.forEach {
                sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
                sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
                sumSale += NumberUtils.getIntOrZero(it.value.todaysAmount)
                sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
                if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                    sumBalanceDue += NumberUtils.getIntOrZero(it.value.balanceDue)
                }
            }

            val loadedKg = NumberUtils.getDoubleOrZero(SingleAttributedData.getRecords().actualLoadKg)
            val shortage = (loadedKg - sumKg) * 100 / loadedKg

            totalPcElement.text = "$sumPc"
            totalKgElement.text = "%.3f".format(sumKg)
            totalSaleElement.text = "$sumSale"
            metadataObj.daySale = sumSale.toString()

            totalShortageElement.text = "▼ ${"%.3f".format(shortage)} kg"
            totalCollectedElement.text = "$sumAmountCollected"
            totalBalanceDueElement.text = "$sumBalanceDue"
            SingleAttributedData.saveToLocal(metadataObj)

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
            gatherFuelData()
            DeliverToCustomerDataHandler.deleteData()

            saveSingleAttributeData()
            saveDeliveryData()
            SingleAttributedData.getRecords(false)
            DeliverToCustomerDataHandler.get<DeliverToCustomerDataModel>()
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
        val obj = SingleAttributedData.getRecords()
        obj.vehicle_finalKm = NumberUtils.getIntOrZero(finalKmElement.text.toString()).toString()
        obj.labour_expenses = NumberUtils.getIntOrZero(labourExpensesElement.text.toString()).toString()
        obj.extra_expenses = NumberUtils.getIntOrZero(extraExpensesElement.text.toString()).toString()
        obj.actualLoadKg = loadKgElement.text.toString()
        obj.actualLoadPc = loadPcElement.text.toString()

        SingleAttributedData.saveToLocal(obj)
    }

    private fun saveSingleAttributeData() {
        Thread {
            SingleAttributedData.save(SingleAttributedData.getRecords())
            setSaveProgressBar(10)
        }.start()
    }

    private fun updateRefuelingUIDetails() {
        val mileageLabel = findViewById<TextView>(R.id.one_shot_delivery_refueling_mileage)
        val refuelingKmDiffLabel = findViewById<TextView>(R.id.one_shot_delivery_refueling_km_diff)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)
        val refuelingDetailsContainer = findViewById<LinearLayout>(R.id.osd_refuel_container)
        val refuelingKmContainer = findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_km_container)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)

        refuelingDetailsContainer.visibility = if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
        didTankFullElement.visibility = if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
        refuelingKmContainer.visibility = if (didTankFullElement.isChecked) View.VISIBLE else View.GONE

        refuelingKmDiffLabel.text = if (didTankFullElement.isChecked) RefuelingUtils.getKmDifferenceForRefueling(refuelingKmElement.text.toString()).toString() else "N/A"
        mileageLabel.text = if (didTankFullElement.isChecked) getMileage() + " km/L" else "N/A"

        LogMe.log("KM: " + refuelingKmElement.text.toString())
        LogMe.log("Mileage: " + getMileage())
    }

    private fun getMileage(): String {
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)

        val refuelingKM = refuelingKmElement.text.toString()
        val refuelingQty = refuelingQtyElement.text.toString()
        LogMe.log("Converting String: " + RefuelingUtils.getMileage(refuelingKM, refuelingQty))
        return if (NumberUtils.getDoubleOrZero(refuelingQty) > 0.0)
            RefuelingUtils.getMileage(refuelingKM, refuelingQty)
        else
            "N/A"
    }

    private fun gatherFuelData() {
        val obj = SingleAttributedData.getRecords()
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        obj.refueling_km = ""
        obj.refueling_prevKm = ""
        obj.refuel_mileage = ""
        obj.refueling_amount = ""
        obj.refueling_qty = ""
        obj.refueling_amount = ""
        obj.did_refueled = didRefuelElement.isChecked.toString()

        if (didRefuelElement.isChecked) {
            val refuelQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
            val refuelAmountElement = findViewById<EditText>(R.id.osd_refuel_amount)
            val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
            val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)

            obj.did_refueled = didRefuelElement.isChecked.toString()
            obj.refueling_isFullTank = didTankFullElement.isChecked.toString()
            obj.refueling_qty = refuelQtyElement.text.toString()
            obj.refueling_amount = refuelAmountElement.text.toString()

            if (didTankFullElement.isChecked) {
                obj.refueling_km = refuelingKmElement.text.toString()
                obj.refueling_prevKm = RefuelingUtils.getPreviousRefuelingKM()
                obj.refuel_mileage = getMileage()
            } else {
                obj.refueling_km = ""
                obj.refueling_prevKm = ""
                obj.refuel_mileage = ""
            }
        }

        SingleAttributedData.saveToLocal(obj)
    }

    private fun saveDeliveryData() {
        var eachStep = 0

        val allDeliveredRecords: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
        allDeliveredRecords.putAll(deliveryMapOrderedCustomers)
        allDeliveredRecords.putAll(deliveryMapUnOrderedCustomers)

        // save locally
        allDeliveredRecords.forEach {
            DeliverToCustomerDataHandler.saveToLocal(it.value)
        }

        // save to server
        allDeliveredRecords.forEach {
            LogMe.log(it.value.name + ":: deliveredKg:" + it.value.deliveredKg)
            LogMe.log(it.value.name + ":: paid:" + it.value.paid)
            if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                it.value.deliveryStatus = "DELIVERED"
                DeliverToCustomerDataHandler.saveToServer(it.value)
                if (eachStep + 10 < 100) {
                    eachStep += 10
                } else {
                    eachStep = 100
                }
                runOnUiThread { setSaveProgressBar(eachStep) }
            }
        }
        runOnUiThread { setSaveProgressBar(100) }
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