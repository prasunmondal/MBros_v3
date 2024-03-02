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
    private lateinit var finalKmElementSecondPart: EditText
    private lateinit var finalKmElementFirstPart: EditText
    private lateinit var salaryPaidElement: EditText
    private lateinit var extraExpensesElement: EditText
    private lateinit var loadPcElement: EditText
    private lateinit var loadKgElement: EditText
    private lateinit var loadAvgWtElement: TextView
    private lateinit var meteredKm: MeteredNumbers
    private lateinit var meteredFuelKms: MeteredNumbers
    private lateinit var refuelQtyElement: EditText
    private lateinit var refuelAmountElement: EditText

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
            runOnUiThread {
                showOrders()
                initializeOtherExpensesUI()
                OSDLoadInfo.updateRelatedFields_LoadPcKg(loadPcElement, loadKgElement, loadAvgWtElement)
                initiallizeRefuelUI()
                updateKmRelatedCosts()
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
        salaryPaidElement = findViewById(R.id.osd_salary_paid)
        extraExpensesElement = findViewById(R.id.one_shot_delivery_extra_expenses)
        loadPcElement = findViewById(R.id.one_shot_delivery_pc)
        loadKgElement = findViewById(R.id.one_shot_delivery_kg)
        loadAvgWtElement = findViewById(R.id.osd_loading_avg_wt)

        val refuelingKmElementPart1 = findViewById<EditText>(R.id.one_shot_delivery_refueling_km_part1)
        val refuelingKmElementPart2 = findViewById<EditText>(R.id.one_shot_delivery_refueling_km_part2)
        refuelQtyElement = findViewById(R.id.one_shot_delivery_fuel_quantity)
        refuelAmountElement = findViewById(R.id.osd_refuel_amount)
        meteredFuelKms = MeteredNumbers(refuelingKmElementPart1, refuelingKmElementPart2, 3)
        meteredFuelKms.setListeners {updateRefuelingUIDetails()}

        finalKmElementFirstPart = findViewById(R.id.one_shot_delivery_trip_end_km_first_part)
        finalKmElementSecondPart = findViewById(R.id.one_shot_delivery_trip_end_km_second_part)
        meteredKm = MeteredNumbers(finalKmElementFirstPart, finalKmElementSecondPart, 3)
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

        runOnUiThread {
            meteredFuelKms.setNumber(NumberUtils.getIntOrZero(RefuelingUtils.getPreviousRefuelingKM()), true)
        }

        OSDLoadInfo.initializeUI(this, loadPcElement, loadKgElement, loadAvgWtElement)
        didRefuelElement.setOnCheckedChangeListener { _, isChecked ->
            val obj = SingleAttributedDataUtils.getRecords()
            obj.did_refueled = isChecked.toString()
            SingleAttributedDataUtils.saveToLocal(obj)
            updateRefuelingUIDetails()
        }

        didTankFullElement.setOnCheckedChangeListener { _, isChecked ->
            SingleAttributedDataUtils.saveAttributeToLocal(SingleAttributedDataModel::refueling_isFullTank, isChecked.toString())
            updateRefuelingUIDetails()
        }

        refuelQtyElement.doOnTextChanged { text, start, before, count ->
            updateRefuelingUIDetails()
            updateFuelRate()
        }
        refuelAmountElement.doOnTextChanged { text, start, before, count ->
            updateFuelRate()
        }
    }

    private fun initializeOtherExpensesUI() {
        val salaryDivisionElement = findViewById<TextView>(R.id.osd_salary_division)

        val vehiclePrevKm: Int = NumberUtils.getIntOrZero(SingleAttributedDataUtils.getRecords().vehicle_finalKm)

        meteredKm.setNumber(vehiclePrevKm, true)
        meteredKm.setListeners { updateKmRelatedCosts() }

        val salaryPaid = NumberUtils.getIntOrZero(SingleAttributedDataUtils.getRecords().labour_expenses) + NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DRIVER_SALARY))
        UIUtils.setUIElementValue(salaryPaidElement, salaryPaid.toString())
        UIUtils.setUIElementValue(extraExpensesElement, SingleAttributedDataUtils.getRecords().extra_expenses)
        UIUtils.setUIElementValue(salaryDivisionElement, SingleAttributedDataUtils.getRecords().salaryDivision.replace("#", "  #  "))
    }

    private fun updateKmRelatedCosts() {
        val prevKmElement = findViewById<TextView>(R.id.osd_prev_km)

        // if second part is not 3 digits, nothing is processed
        val currentKmOnUI = meteredKm.getNumber().toString()

        val kmDiffElement = findViewById<TextView>(R.id.osd_km_diff)
        val kmCostElement = findViewById<TextView>(R.id.osd_km_cost)

        val currentKm = NumberUtils.getIntOrZero(currentKmOnUI)
        val prevKm = DaySummaryUtils.getPrevTripEndKm()
        prevKmElement.text = prevKm.toString()

        if (currentKm < prevKm) {
            kmDiffElement.text = "N/A"
            kmCostElement.text = "N/A"
            return
        }

        val kmDiff = DeliveryCalculations.getKmDiff(currentKmOnUI)
        val kmCost = DeliveryCalculations.getKmCost(currentKmOnUI)

        val singleDataObj = SingleAttributedDataUtils.getRecords()
        singleDataObj.vehicle_finalKm = currentKm.toString()
        SingleAttributedDataUtils.saveToLocal(singleDataObj)

        kmDiffElement.text = kmDiff.toString()
        kmCostElement.text = kmCost.toString()
    }

    private fun initiallizeRefuelUI() {
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)

        UIUtils.setUIElementValue(didRefuelElement, SingleAttributedDataUtils.getRecords().did_refueled)
        UIUtils.setUIElementValue(didTankFullElement, SingleAttributedDataUtils.getRecords().refueling_isFullTank)
        UIUtils.setUIElementValue(refuelQtyElement, SingleAttributedDataUtils.getRecords().refueling_qty)
        UIUtils.setUIElementValue(refuelAmountElement, SingleAttributedDataUtils.getRecords().refueling_amount)

        updateRefuelingUIDetails()
    }

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
        findViewById<LinearLayout>(R.id.one_shot_delivery_unordered_customers_entry_container).addView(value)
    }

    private fun showOrders() {
        var t = showOrders(deliverRecords, R.id.one_shot_delivery_ordered_customers_entry_container)
        findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).removeAllViews()

        t.forEach { (key, value) ->
            OSDDeliveryEntryInfo.updateEntry(this, key, value, false)
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
            gatherFuelData()
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
        val salaryPaid = (NumberUtils.getIntOrZero(salaryPaidElement.text.toString()) - NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DRIVER_SALARY)))
        obj.vehicle_finalKm = meteredKm.getNumber().toString()
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

    private fun getFuelRate(): Double {
        val refuelingQty = NumberUtils.getDoubleOrZero(refuelQtyElement.text.toString())
        val refuelingAmount = NumberUtils.getDoubleOrZero(refuelAmountElement.text.toString())
        val fuelPrice = refuelingAmount / refuelingQty
        if(refuelingQty == 0.0)
            return 0.0
        return NumberUtils.roundOff2places(fuelPrice)
    }

    private fun updateFuelRate() {
        val oilRateLabel = findViewById<TextView>(R.id.one_shot_delivery_refueling_oil_rate_per_litre)
        val fuelRate = getFuelRate()
        oilRateLabel.text = " ₹ $fuelRate"

        // Show red background if oil rate is not within limits
        val upperRateLimit = NumberUtils.getIntOrZero(AppConstants.get(AppConstants.FUEL_OIL_RATE_UPPER_LIMIT))
        val lowerRateLimit = NumberUtils.getIntOrZero(AppConstants.get(AppConstants.FUEL_OIL_RATE_LOWER_LIMIT))
        val fuelRateInInt = NumberUtils.getIntOrZero(fuelRate.toInt().toString())
        if(fuelRateInInt < lowerRateLimit || fuelRateInInt > upperRateLimit) {
            oilRateLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.osd_fuel_rate_not_matching))
            oilRateLabel.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {

            oilRateLabel.setBackgroundColor(0x00000000)
            oilRateLabel.setTextColor(ContextCompat.getColor(this, R.color.osd_fuel_non_interactive_ok_text_color))
        }
    }
    private fun updateRefuelingUIDetails() {
        if(meteredFuelKms.getNumber() !=  null) {
            val mileageLabel = findViewById<TextView>(R.id.one_shot_delivery_refueling_mileage)
            val refuelingKmDiffLabel =
                findViewById<TextView>(R.id.one_shot_delivery_refueling_km_diff)
            val refuelingKmElement =
                findViewById<EditText>(R.id.one_shot_delivery_refueling_km_part1)
            val refuelingDetailsContainer = findViewById<LinearLayout>(R.id.osd_refuel_container)
            val refuelingKmContainer =
                findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_km_container)
            val didTankFullElement =
                findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
            val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)

            refuelingDetailsContainer.visibility =
                if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
            didTankFullElement.visibility =
                if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
            refuelingKmContainer.visibility =
                if (didTankFullElement.isChecked) View.VISIBLE else View.GONE

            refuelingKmDiffLabel.text =
                if (didTankFullElement.isChecked)
                    RefuelingUtils.getKmDifferenceForRefueling(meteredFuelKms.getNumber()!!).toString()
                else "N/A"
            mileageLabel.text = if (didTankFullElement.isChecked) getMileage() + " km/L" else "N/A"

            LogMe.log("KM: " + meteredFuelKms.getNumber())
            LogMe.log("Mileage: " + getMileage())

            if (RefuelingUtils.getKmDifferenceForRefueling(meteredFuelKms.getNumber()!!) > 0) {
                // add general kms from petrol pump to home, and set the total kms accordingly
                val refuelingKm = meteredFuelKms.getNumber()!!
                val addKmToFuelKmToGetFinalKm =
                    NumberUtils.getIntOrZero(AppConstants.get(AppConstants.ADD_TO_FUELING_KMS_TO_GET_FINAL_KM))
                meteredKm.setNumber(refuelingKm + addKmToFuelKmToGetFinalKm, false)
            }
        }
    }

    private fun getMileage(): String {
        val refuelingKM = meteredFuelKms.getNumber()!!
        val refuelingQty = refuelQtyElement.text.toString()
        LogMe.log("Converting String: " + RefuelingUtils.getMileage(refuelingKM, refuelingQty))
        return if (NumberUtils.getDoubleOrZero(refuelingQty) > 0.0)
            RefuelingUtils.getMileage(refuelingKM, refuelingQty)
        else
            "N/A"
    }

    private fun gatherFuelData() {
        val obj = SingleAttributedDataUtils.getRecords()
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        obj.refueling_km = ""
        obj.refueling_prevKm = ""
        obj.refuel_mileage = ""
        obj.refueling_amount = ""
        obj.refueling_qty = ""
        obj.refueling_amount = ""
        obj.did_refueled = didRefuelElement.isChecked.toString()

        if (didRefuelElement.isChecked) {
            val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)

            obj.did_refueled = didRefuelElement.isChecked.toString()
            obj.refueling_isFullTank = didTankFullElement.isChecked.toString()
            obj.refueling_qty = refuelQtyElement.text.toString()
            obj.refueling_amount = refuelAmountElement.text.toString()

            if (didTankFullElement.isChecked) {
                obj.refueling_km = meteredFuelKms.getNumber().toString()
                obj.refueling_prevKm = RefuelingUtils.getPreviousRefuelingKM()
                obj.refuel_mileage = getMileage()
            } else {
                obj.refueling_km = ""
                obj.refueling_prevKm = ""
                obj.refuel_mileage = ""
            }
        }

        SingleAttributedDataUtils.saveToLocal(obj)
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