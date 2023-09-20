package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.SMSDetails.SendSMSDetailsUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerConfig
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling
import java.util.*


class OneShotDelivery : AppCompatActivity() {

    var deliveryMapOrderedCustomers: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
    var deliveryMapUnOrderedCustomers: MutableMap<String, DeliverToCustomerDataModel> = mutableMapOf()
    var uiMaps: MutableMap<String, View> = mutableMapOf()
    lateinit var saveOneSortDeliveryButton: Button
    lateinit var deleteDeliveryDataButton: Button
    lateinit var sidebarIconLoadDetails: ImageView
    lateinit var sidebarIconDelivery: ImageView
    lateinit var sidebarIconRefuel: ImageView
    lateinit var sidebarIconOtherExpenses: ImageView
    lateinit var refuelContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this)
        AppUtils.logError()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getSupportActionBar()?.hide()

        saveOneSortDeliveryButton = findViewById(R.id.one_shot_delivery_save_data_btn)
        deleteDeliveryDataButton = findViewById(R.id.osd_delete_delivery_data)

        getSMSPermission()
        Thread {
            populateDeliveryMap()
            updateSingleAttributedDataOnUI()
            showOrders()
            initiallizeUI()
            runOnUiThread {
                initiallizeOtherExpensesUI()
                updateRelatedFields_LoadPcKg()
                initiallizeRefuelUI()
                updateKmRelatedCosts()
            }
        }.start()
    }

    fun onClickSidebarIconRefuel(view: View) {
        val scrollview = findViewById<ScrollView>(R.id.osd_scrollview)
        scrollToRow(scrollview, refuelContainer, findViewById<Switch>(R.id.one_shot_delivery_did_refuel))
//        refuelContainer = findViewById(R.id.osd_refuel_container)
//        scrollview.scrollTo(0, refuelContainer.y.toInt())
    }

    private fun scrollToRow(scrollView: ScrollView, linearLayout: LinearLayout, textViewToShow: View) {
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
        val loadPcElement = findViewById<EditText>(R.id.one_shot_delivery_pc)
        val loadKgElement = findViewById<EditText>(R.id.one_shot_delivery_kg)
        val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
        val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)
        sidebarIconDelivery = findViewById(R.id.osd_sidebar_icon_delivery)
        sidebarIconLoadDetails = findViewById(R.id.osd_sidebar_icon_load_details)
        sidebarIconRefuel = findViewById(R.id.osd_sidebar_icon_refuel)
        sidebarIconOtherExpenses = findViewById(R.id.osd_sidebar_icon_other_expenses)
        refuelContainer = findViewById(R.id.osd_refuel_container)

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

        loadPcElement.doOnTextChanged { text, start, before, count ->
            record.actualLoadPc = loadPcElement.text.toString()
            SingleAttributedData.saveToLocal(record)
            updateRelatedFields_LoadPcKg()
            updateTotals()
        }

        loadKgElement.doOnTextChanged { text, start, before, count ->
            record.actualLoadKg = loadKgElement.text.toString()
            SingleAttributedData.saveToLocal(record)
            updateRelatedFields_LoadPcKg()
            updateTotals()
        }

        loadPriceElement.doOnTextChanged { text, start, before, count ->
            record.finalFarmRate = loadPriceElement.text.toString()
            SingleAttributedData.saveToLocal(record)
            updateRates()
        }

        loadBufferElement.doOnTextChanged { text, start, before, count ->
            record.bufferRate = loadBufferElement.text.toString()
            SingleAttributedData.saveToLocal(record)
            updateRates()
        }


    }

    private fun updateRelatedFields_LoadPcKg() {
        val loadAvgWtElement = findViewById<TextView>(R.id.osd_loading_avg_wt)
        val totalPcElement = findViewById<TextView>(R.id.one_shot_delivery_pc)
        val totalKgElement = findViewById<TextView>(R.id.one_shot_delivery_kg)

        var avgWt = "N/A"
        try {
            avgWt = NumberUtils.roundOff3places(NumberUtils.getDoubleOrZero(totalKgElement.text.toString()) / NumberUtils.getIntOrZero(totalPcElement.text.toString())).toString()
        } catch (_: Exception) {
            LogMe.log("Error while getting avg")
        } finally {
            LogMe.log(avgWt)
            loadAvgWtElement.text = avgWt
        }
    }

    private fun initiallizeOtherExpensesUI() {
        val tripEndKmElement = findViewById<EditText>(R.id.one_shot_delivery_trip_end_km)
        val labourExpenseElement = findViewById<EditText>(R.id.one_shot_delivery_labour_expenses)
        val extraExpensesElement = findViewById<EditText>(R.id.one_shot_delivery_extra_expenses)
        val salaryDivisionElement = findViewById<TextView>(R.id.osd_salary_division)

        UIUtils.setUIElementValue(tripEndKmElement, SingleAttributedData.getRecords().vehicle_finalKm)
        UIUtils.setUIElementValue(labourExpenseElement, SingleAttributedData.getRecords().labour_expenses)
        UIUtils.setUIElementValue(extraExpensesElement, SingleAttributedData.getRecords().extra_expenses)
        UIUtils.setUIElementValue(salaryDivisionElement, SingleAttributedData.getRecords().salaryDivision.replace("#", "  #  "))

        tripEndKmElement.doOnTextChanged { text, start, before, count ->
            updateKmRelatedCosts()
        }

    }

    private fun updateKmRelatedCosts() {
        val tripEndKmElement = findViewById<EditText>(R.id.one_shot_delivery_trip_end_km)
        val currentKmOnUI = tripEndKmElement.text.toString()
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
        val tripEndKmElement = findViewById<EditText>(R.id.one_shot_delivery_trip_end_km)

        UIUtils.setUIElementValue(didRefuelElement, SingleAttributedData.getRecords().did_refueled)
        UIUtils.setUIElementValue(didTankFullElement, SingleAttributedData.getRecords().refueling_isFullTank)
        UIUtils.setUIElementValue(refuelingQtyElement, SingleAttributedData.getRecords().refueling_qty)
        UIUtils.setUIElementValue(refuelingKmElement, SingleAttributedData.getRecords().refueling_km)
        UIUtils.setUIElementValue(refuelingAmountElement, SingleAttributedData.getRecords().refueling_amount)

        refuelingKmElement.doOnTextChanged { text, start, before, count ->
            val refuelingKm = NumberUtils.getIntOrZero(refuelingKmElement.text.toString())
            tripEndKmElement.setText((refuelingKm + 7).toString())
        }

        updateRefuelingUIDetails()
    }

    private fun populateDeliveryMap() {
        deliveryMapOrderedCustomers = mutableMapOf()
        val listOfOrderedCustomers = GetCustomerOrders.getListOfOrderedCustomers()
        listOfOrderedCustomers.forEach {
            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = it.orderedPc,
                orderedKg = it.orderedKg,
                rate = "${CustomerData.getDeliveryRate(it.name)}",
                prevDue = CustomerData.getLastDue(it.name),
                deliveryStatus = "DELIVERING"
            )

            deliveryMapOrderedCustomers[it.name] = deliverCustomersOrders
        }

        deliveryMapUnOrderedCustomers = mutableMapOf()
        val listOfUnOrderedCustomers = GetCustomerOrders.getListOfUnOrderedCustomers()
        listOfUnOrderedCustomers.forEach {
            val deliverCustomersOrders = DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = "0",
                orderedKg = "0",
                rate = "${CustomerData.getDeliveryRate(it.name)}",
                prevDue = CustomerData.getLastDue(it.name),
                deliveryStatus = "DELIVERING"
            )

            deliveryMapUnOrderedCustomers[it.name] = deliverCustomersOrders
        }
    }

    private fun showOrders() {

        runOnUiThread {
            var t = showOrders(deliveryMapOrderedCustomers, R.id.one_shot_delivery_ordered_customers_entry_container)
            var t2 = showOrders(deliveryMapUnOrderedCustomers, R.id.one_shot_delivery_unordered_customers_entry_container)

            findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).removeAllViews()
            t.forEach { key, value ->
                updateEntry(key, value)
                findViewById<LinearLayout>(R.id.one_shot_delivery_ordered_customers_entry_container).addView(value)
            }

            findViewById<LinearLayout>(R.id.one_shot_delivery_unordered_customers_entry_container).removeAllViews()
            t2.forEach { key, value ->
                updateEntry(key, value)
                findViewById<LinearLayout>(R.id.one_shot_delivery_unordered_customers_entry_container).addView(value)
            }
        }
    }

    var entrynumber = 1
    private fun showOrders(listOfCustomers: MutableMap<String, DeliverToCustomerDataModel>, container: Int): MutableMap<DeliverToCustomerDataModel, View> {
        entrynumber = 1
        val listContainer = findViewById<LinearLayout>(container)
        val entryMap: MutableMap<DeliverToCustomerDataModel, View> = mutableMapOf()

        listContainer.removeAllViews()

        listOfCustomers.forEach { order ->
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_one_shot_delivery_fragment, null)

            val nameElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_name)
            val rateElementContainer = entry.findViewById<TextInputLayout>(R.id.osd_rate_for_customer_container)
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val pcElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val moreDetailsContainer = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)
            val sendSMSBtn = entry.findViewById<TextView>(R.id.osd_fragment_send_details)
            rateElementContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE

            nameElement.text = order.value.name
            balanceElement.text = order.value.prevDue
            val deliveryRecord = DeliverToCustomerActivity.getDeliveryRecord(order.value.name)
            if (deliveryRecord != null) {
                pcElement.setText(deliveryRecord.deliveredPc)
                kgElement.text = deliveryRecord.deliveredKg
                paidElement.text = deliveryRecord.paid
            }

            if (SendSMSDetailsUtils.getSendSMSDetailsNumber(order.value.name) != null) {
                sendSMSBtn.visibility = View.VISIBLE
                sendSMSBtn.setOnClickListener {
                    val smsNumber = CustomerKYC.getCustomerByEngName(order.value.name)!!.smsNumber
                    val t = DateUtils.getDate(order.value.timestamp)
                    val formattedDate = DateUtils.getDateInFormat(t!!, "dd/MM/yyyy")
                    val smsText = CustomerKYC.getCustomerByEngName(order.value.name)!!.smsText
                        .replace("<date>", formattedDate)
                        .replace("<pc>", pcElement.text.toString())
                        .replace("<kg>", kgElement.text.toString())
                        .replace("<paidAmount>", paidElement.text.toString())
                        .replace("<rate>", rateElement.text.toString())
                        .replace("<balanceAmount>", balanceElement.text.toString())

                    SMSUtils.sendSMS(baseContext, smsText, smsNumber)
                    Toast.makeText(this, "SMS Sent: $smsNumber", Toast.LENGTH_LONG).show()
                }
            }

            rateElement.setText("${CustomerData.getDeliveryRate(order.value.name)}")
            fragmentUpdateCustomerWiseRateView(order, entry)

            rateElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order.value, entry)
                fragmentUpdateCustomerWiseRateView(order, entry)
            }

            pcElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order.value, entry)
            }

            kgElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order.value, entry)
            }

            paidElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order.value, entry)
            }

            balanceElement.setOnClickListener {
                if (moreDetailsContainer.visibility == View.VISIBLE) {
                    moreDetailsContainer.visibility = View.GONE
                } else {
                    moreDetailsContainer.visibility = View.VISIBLE
                }
                updateDetailedInfo(order.value, entry)
            }

            val recordContainer = entry.findViewById<CardView>(R.id.one_shot_delivery_fragment_record_container)
            var cardColor = ContextCompat.getColor(this, R.color.one_shot_delivery_odd_card_color)
            if (entrynumber % 2 == 0) {
                cardColor = ContextCompat.getColor(this, R.color.one_shot_delivery_even_card_color)
            }
            entrynumber++
            recordContainer.setBackgroundColor(cardColor)

//            listContainer.addView(entry)
            entryMap.put(order.value, entry)
            uiMaps[order.value.name] = entry
        }
        return entryMap
    }

    private fun getSMSPermission() {
        val PERMISSION_REQUEST_CODE = 123
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to SEND_SMS - requesting it")
                val permissions = arrayOf(android.Manifest.permission.SEND_SMS)
                requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun fragmentUpdateCustomerWiseRateView(order: Map.Entry<String, DeliverToCustomerDataModel>, entry: View) {
        val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
        if (NumberUtils.getIntOrZero(rateElement.text.toString()) != CustomerData.getCustomerDefaultRate(order.value.name)) {
            rateElement.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            rateElement.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            rateElement.setBackgroundColor(0x00000000)
            rateElement.setTextColor(rateElement.textColors.defaultColor)
        }
    }

    private fun updateRates() {
        uiMaps.forEach {
            val rate = CustomerData.getCustomerDefaultRate(it.key)
            val rateElement = it.value.findViewById<TextView>(R.id.osd_rate_for_customer)
            rateElement.text = rate.toString()
        }
    }

    private fun updateEntry(order: DeliverToCustomerDataModel, entry: View) {
        order.deliveredKg = getKgForEntry(entry).toString()
        order.deliveredPc = getPcForEntry(entry).toString()
        order.todaysAmount = getTodaysSaleAmountForEntry(entry).toString()
        order.paid = getPaidAmountForEntry(entry).toString()
        order.rate = getRateForEntry(entry).toString()
        order.totalDue = "${NumberUtils.getIntOrZero(order.prevDue) + getTodaysSaleAmountForEntry(entry)}"
        order.balanceDue = "${NumberUtils.getIntOrZero(order.prevDue) + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)}"

        val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)

        balanceElement.text = getDueBalance(order, entry).toString()
        updateTotals()
        updateDetailedInfo(order, entry)
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

    private fun updateDetailedInfo(order: DeliverToCustomerDataModel, entry: View) {
        val container = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)

        if (container.visibility == View.VISIBLE) {
            val prevDue = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_prev_due)
            val kg = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_kg)
            val rate = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_rate)
            val todaysSale = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_sale_total)
            val total = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_total_due)
            val paid = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_paid_amount)
            val balanceDue = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_balance_due)

            prevDue.text = "₹ ${order.prevDue}"
            kg.text = "${order.deliveredKg} kg"
            rate.text = "₹ ${order.rate}"
            todaysSale.text = "₹ ${order.todaysAmount}"
            total.text = "₹ ${order.totalDue}"
            paid.text = "₹ ${order.paid}"
            balanceDue.text = "₹ ${order.balanceDue}"
        }
    }

    private fun getRateForEntry(entry: View): Int {
        val rate = entry.findViewById<TextView>(R.id.osd_rate_for_customer).text.toString()
        if (rate.isEmpty())
            return 0
        return rate.toInt()
    }

    private fun getPcForEntry(entry: View): Int {
        val pc = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc).text.toString()
        if (pc.isEmpty())
            return 0
        return pc.toInt()
    }

    private fun getKgForEntry(entry: View): Double {
        val kg = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg).text.toString()
        if (kg.isEmpty())
            return 0.0
        return kg.toDouble()

    }

    private fun getPaidAmountForEntry(entry: View): Int {
        val paid = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid).text.toString()
        if (paid.isEmpty())
            return 0
        return paid.toInt()
    }

    private fun getTodaysSaleAmountForEntry(entry: View): Int {
        val kg = getKgForEntry(entry)
        val rate = getRateForEntry(entry)
        val roundUpOffset = 0.000001
        return (kg * rate + roundUpOffset).toInt()
    }

    private fun getDueBalance(order: DeliverToCustomerDataModel, entry: View): Int {
        val prevBal = order.prevDue
        return NumberUtils.getIntOrZero(prevBal) + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)
    }

    private fun updateSingleAttributedDataOnUI() {
        val loadedPc = findViewById<TextView>(R.id.one_shot_delivery_pc)
        val loadedKg = findViewById<TextView>(R.id.one_shot_delivery_kg)
        runOnUiThread {
            if (!isSendLoadInfoEnabled()) {
                findViewById<TextView>(R.id.osd_btn_send_load_info_to_account_payee).visibility = View.GONE
            }

            loadedPc.text = SingleAttributedData.getRecords().actualLoadPc
            loadedKg.text = SingleAttributedData.getRecords().actualLoadKg
            findViewById<TextView>(R.id.osd_company_name).text = SingleAttributedData.getRecords().load_account
        }

        if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_FARM_RATE)) {
            val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
            runOnUiThread {
                loadPriceElement.setText(SingleAttributedData.getRecords().finalFarmRate)
            }
        } else {
            runOnUiThread {
                findViewById<TextInputLayout>(R.id.osd_farm_rate_container).visibility = View.GONE
            }
        }

        if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_BUFFER_RATE)) {
            val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)
            runOnUiThread {
                loadBufferElement.setText(SingleAttributedData.getRecords().bufferRate)
            }
        } else {
            runOnUiThread {
                findViewById<TextInputLayout>(R.id.osd_buffer_price_container).visibility = View.GONE
            }
        }
    }

    private fun updateTotals() {
        val metadataObj = SingleAttributedData.getRecords()
        val totalPcElement = findViewById<TextView>(R.id.one_shot_delivery_total_pc)
        val totalKgElement = findViewById<TextView>(R.id.one_shot_delivery_total_kg)
        val totalSaleElement = findViewById<TextView>(R.id.one_shot_delivery_total_sale)
        val totalShortageElement = findViewById<TextView>(R.id.one_shot_delivery_total_shortage)
        val totalCollectedElement = findViewById<TextView>(R.id.one_shot_delivery_total_collected_amount)
        val totalBalanceDueElement = findViewById<TextView>(R.id.one_shot_delivery_total_balance_due)

        var sumPc = 0
        var sumKg = 0.0
        var sumSale = 0
        var sumAmountCollected = 0
        var sumBalanceDue = 0

        deliveryMapOrderedCustomers.forEach {
            sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
            sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
            sumSale += NumberUtils.getIntOrZero(it.value.todaysAmount)
            sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
            if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                sumBalanceDue += NumberUtils.getIntOrZero(it.value.balanceDue)
            }
        }

        deliveryMapUnOrderedCustomers.forEach {
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
        totalKgElement.text = "${"%.3f".format(sumKg)}"
        totalSaleElement.text = "$sumSale"
        metadataObj.daySale = sumSale.toString()

        totalShortageElement.text = "▼ ${"%.3f".format(shortage)} kg"
        totalCollectedElement.text = "$sumAmountCollected"
        totalBalanceDueElement.text = "$sumBalanceDue"
        SingleAttributedData.saveToLocal(metadataObj)

        updateHiddenData()
    }

    fun onClickSaveOneShotDeliveryDataBtn(view: View) {
        Thread {
            runOnUiThread()
            {
                findViewById<ProgressBar>(R.id.osd_save_progress_bar).visibility = View.VISIBLE
                saveOneSortDeliveryButton.isEnabled = false
                saveOneSortDeliveryButton.alpha = .5f
                saveOneSortDeliveryButton.isClickable = false;
            }
            gatherSingleAttributedData()
            gatherFuelData()
            saveSingleAttributeData()
            DeliverToCustomerDataHandler.deleteAllData()
            saveDeliveryData()
            SingleAttributedData.getRecords(false)
            DeliverToCustomerDataHandler.get(false)
            runOnUiThread()
            {
                saveOneSortDeliveryButton.isEnabled = true
                saveOneSortDeliveryButton.alpha = 1.0f;
                saveOneSortDeliveryButton.isClickable = true;
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
        val finalKmElement = findViewById<EditText>(R.id.one_shot_delivery_trip_end_km)
        val labourExpensesElement = findViewById<EditText>(R.id.one_shot_delivery_labour_expenses)
        val extraExpensesElement = findViewById<EditText>(R.id.one_shot_delivery_extra_expenses)

        val obj = SingleAttributedData.getRecords()
        obj.vehicle_finalKm = NumberUtils.getIntOrZero(finalKmElement.text.toString()).toString()
        obj.labour_expenses = NumberUtils.getIntOrZero(labourExpensesElement.text.toString()).toString()
        obj.extra_expenses = NumberUtils.getIntOrZero(extraExpensesElement.text.toString()).toString()

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
        val refuelingDetailsContainer = findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_details_container)
        val refuelingKmContainer = findViewById<TextInputLayout>(R.id.one_shot_delivery_refueling_km_container)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)

        refuelingDetailsContainer.visibility = if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
        didTankFullElement.visibility = if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
        refuelingKmContainer.visibility = if (didTankFullElement.isChecked) View.VISIBLE else View.GONE

        refuelingKmDiffLabel.text = if (didTankFullElement.isChecked) Refueling.getKmDifferenceForRefueling(refuelingKmElement.text.toString()).toString() else "N/A"
        mileageLabel.text = if (didTankFullElement.isChecked) getMileage() + " km/L" else "N/A"

        LogMe.log("KM: " + refuelingKmElement.text.toString())
        LogMe.log("Mileage: " + getMileage())
    }

    private fun getMileage(): String {
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)

        val refuelingKM = refuelingKmElement.text.toString()
        val refuelingQty = refuelingQtyElement.text.toString()
        LogMe.log("Converting String: " + Refueling.getMileage(refuelingKM, refuelingQty))
        return if (NumberUtils.getDoubleOrZero(refuelingQty) > 0.0)
            Refueling.getMileage(refuelingKM, refuelingQty)
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
                obj.refueling_prevKm = Refueling.getPreviousRefuelingKM()
                obj.refuel_mileage = getMileage()
            } else {
                obj.refueling_km = ""
                obj.refueling_prevKm = ""
                obj.refuel_mileage = ""
            }
        }

        SingleAttributedData.saveToLocal(obj)
    }

    private fun deleteDeliveryDataOnServer() {
        Delete.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.get_db_sheet_id())
            .tabName(DeliverToCustomerConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
            .build().execute()
    }

    private fun saveDeliveryData() {
        var eachStep = 0
        deliveryMapOrderedCustomers.forEach {
            LogMe.log(it.value.name + ":: deliveredKg:" + it.value.deliveredKg)
            LogMe.log(it.value.name + ":: paid:" + it.value.paid)
            if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                it.value.deliveryStatus = "DELIVERED"
                DeliverToCustomerDataHandler.save(it.value)
                if (eachStep + 10 < 100) {
                    eachStep += 10
                } else {
                    eachStep = 100
                }
                runOnUiThread { setSaveProgressBar(eachStep) }
            }
        }
        deliveryMapUnOrderedCustomers.forEach {
            if (NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                it.value.deliveryStatus = "DELIVERED"
                DeliverToCustomerDataHandler.save(it.value)
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
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
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
        val metadata = SingleAttributedData.getRecords()
        val keyFromAppConstantWhatsappNumber = ("WHATSAPP_NUMBER_" + metadata.load_account).uppercase(Locale.ROOT)
        val keyFromAppConstantTextTemplate = ("SEND_LOAD_INFO_TEMPLATE_" + metadata.load_account).uppercase(Locale.ROOT)
        val numberToSendInfo = AppConstants.get(keyFromAppConstantWhatsappNumber)
        val templateToSendInfo = AppConstants.get(keyFromAppConstantTextTemplate)

        val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
        val loadedPc = findViewById<TextView>(R.id.one_shot_delivery_pc)
        val loadedKg = findViewById<TextView>(R.id.one_shot_delivery_kg)
        val text = templateToSendInfo
            .replace("<date>", formattedDate)
            .replace("<loadPc>", loadedPc.text.toString())
            .replace("<loadKg>", loadedKg.text.toString())
            .replace("<loadCompanyName>", metadata.load_companyName)
        Whatsapp.sendMessage(this, numberToSendInfo, text)
    }

    fun isSendLoadInfoEnabled(): Boolean {
        val metadata = SingleAttributedData.getRecords()
        val keyFromAppConstantWhatsappNumber = ("WHATSAPP_NUMBER_" + metadata.load_account).uppercase(Locale.ROOT)
        val keyFromAppConstantTextTemplate = ("SEND_LOAD_INFO_TEMPLATE_" + metadata.load_account).uppercase(Locale.ROOT)
        val numberToSendInfo = AppConstants.get(keyFromAppConstantWhatsappNumber)
        val templateToSendInfo = AppConstants.get(keyFromAppConstantTextTemplate)
        val isSendLoadInfoEnabled = numberToSendInfo.isNotEmpty() && templateToSendInfo.isNotEmpty()

        if (!isSendLoadInfoEnabled) {
            LogMe.log("Send load info disabled. Either '$keyFromAppConstantWhatsappNumber' or '$keyFromAppConstantTextTemplate' is not configured")
        } else {
            LogMe.log("Send load info enabled.")
        }

        return isSendLoadInfoEnabled
    }
}