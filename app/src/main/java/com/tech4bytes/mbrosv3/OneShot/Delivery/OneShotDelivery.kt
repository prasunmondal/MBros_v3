package com.tech4bytes.mbrosv3.OneShot.Delivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.ActivityDeliveringDeliver
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverOrdersConfig
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling

class OneShotDelivery : AppCompatActivity() {

    var deliveryMapOrderedCustomers: MutableMap<String, DeliverCustomerOrders> = mutableMapOf()
    var deliveryMapUnOrderedCustomers: MutableMap<String, DeliverCustomerOrders> = mutableMapOf()
    var uiMaps: MutableMap<String, View> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this)
        AppUtils.logError()
        window.addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar()?.hide()

        populateDeliveryMap()
        updateSingleAttributedDataOnUI()
        showOrders()
        initiallizeUI()
    }

    private fun initiallizeUI() {
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)
        val refuelingDetailsContainer = findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_details_container)
        val refuelingKmContainer = findViewById<TextInputLayout>(R.id.one_shot_delivery_refueling_km_container)
        val loadPcElement = findViewById<EditText>(R.id.one_shot_delivery_pc)
        val loadKgElement = findViewById<EditText>(R.id.one_shot_delivery_kg)
        val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
        val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)


        didRefuelElement.setOnCheckedChangeListener { _, isChecked ->
            val obj = SingleAttributedData.getRecords()
            obj.did_refueled = isChecked.toString()
            SingleAttributedData.saveToLocal(obj)
//            initiallizeUI()
            updateRefuelingUIDetails()
        }

        didTankFullElement.setOnCheckedChangeListener { _, isChecked ->
            SingleAttributedData.saveAttributeToLocal(SingleAttributedData::refueling_isFullTank, isChecked.toString())
//            initiallizeUI()
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
            updateTotals()
        }

        loadKgElement.doOnTextChanged { text, start, before, count ->
            record.actualLoadKg = loadKgElement.text.toString()
            SingleAttributedData.saveToLocal(record)
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

        initiallizeOtherExpensesUI()
        initiallizeRefuelUI()
        updateKmRelatedCosts()
    }

    private fun initiallizeOtherExpensesUI() {
        val tripEndKmElement = findViewById<EditText>(R.id.one_shot_delivery_trip_end_km)
        val labourExpenseElement = findViewById<EditText>(R.id.one_shot_delivery_labour_expenses)
        val extraExpensesElement = findViewById<EditText>(R.id.one_shot_delivery_extra_expenses)

        UIUtils.setUIElementValue(tripEndKmElement, SingleAttributedData.getRecords().vehicle_finalKm)
        UIUtils.setUIElementValue(labourExpenseElement, SingleAttributedData.getRecords().labour_expenses)
        UIUtils.setUIElementValue(extraExpensesElement, SingleAttributedData.getRecords().extra_expenses)

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

        if(currentKm < prevKm) {
            kmDiffElement.text = "N/A"
            kmCostElement.text = "N/A"
            return
        }

        val kmDiff = getKmDiff(currentKmOnUI)
        val kmCost = getKmCost(currentKmOnUI)

        val singleDataObj = SingleAttributedData.getRecords()
        singleDataObj.vehicle_finalKm = currentKm.toString()
        SingleAttributedData.saveToLocal(singleDataObj)

        kmDiffElement.text = kmDiff.toString()
        kmCostElement.text = kmCost.toString()
    }

    private fun getKmDiff(currentKm: String): Int {
        val currentKm = NumberUtils.getIntOrZero(currentKm)
        val prevKm = DaySummary.getPrevTripEndKm()
        return currentKm - prevKm
    }

    private fun getKmCost(currentKm: String): Int {
        return 12 * getKmDiff(currentKm)
    }

    private fun initiallizeRefuelUI() {
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)
        val refuelingAmountElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_amount)

        UIUtils.setUIElementValue(didRefuelElement, SingleAttributedData.getRecords().did_refueled)
        UIUtils.setUIElementValue(didTankFullElement, SingleAttributedData.getRecords().did_refueled)
        UIUtils.setUIElementValue(refuelingQtyElement, SingleAttributedData.getRecords().refueling_qty)
        UIUtils.setUIElementValue(refuelingKmElement, SingleAttributedData.getRecords().refueling_km)
        UIUtils.setUIElementValue(refuelingAmountElement, SingleAttributedData.getRecords().refueling_amount)
        updateRefuelingUIDetails()
    }

    private fun populateDeliveryMap() {
        deliveryMapOrderedCustomers = mutableMapOf()
        GetCustomerOrders.getListOfOrderedCustomers().forEach {
            val deliverCustomersOrders = DeliverCustomerOrders(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = it.orderedPc,
                orderedKg = it.orderedKg,
                rate = it.rate,
                prevDue = it.prevDue,
                deliveryStatus = "DELIVERING")

            deliveryMapOrderedCustomers[it.name] = deliverCustomersOrders
        }

        deliveryMapUnOrderedCustomers = mutableMapOf()
        GetCustomerOrders.getListOfUnOrderedCustomers().forEach {
            val deliverCustomersOrders = DeliverCustomerOrders(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = it.name,
                orderedPc = "0",
                orderedKg = "0",
                rate = it.rate,
                prevDue = CustomerData.getLastDue(it.name),
                deliveryStatus = "DELIVERING")

            deliveryMapUnOrderedCustomers[it.name] = deliverCustomersOrders
        }
    }

    fun showOrders() {
        showOrders(deliveryMapOrderedCustomers, R.id.one_shot_delivery_ordered_customers_entry_container)
        showOrders(deliveryMapUnOrderedCustomers, R.id.one_shot_delivery_unordered_customers_entry_container)
    }

    var entrynumber = 1
    fun showOrders(listOfCustomers: MutableMap<String, DeliverCustomerOrders>, container: Int) {
        entrynumber = 1
        val listContainer = findViewById<LinearLayout>(container)
        listContainer.removeAllViews()

        listOfCustomers.forEach { order ->
            LogMe.log(order.toString())

            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_one_shot_delivery_fragment, null)

            val nameElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_name)
            val rateElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_rate)
            val pcElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val moreDetailsContainer = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)

            nameElement.text = order.value.name
            balanceElement.text = order.value.prevDue
            val deliveryRecord = ActivityDeliveringDeliver.getDeliveryRecord(order.value.name)
            if(deliveryRecord != null) {
                pcElement.text = deliveryRecord.deliveredPc
                kgElement.text = deliveryRecord.deliveredKg
                paidElement.text = deliveryRecord.paid
            }

            LogMe.log(SingleAttributedData.getFinalRateInt().toString())
            LogMe.log(SingleAttributedData.getBufferRateInt().toString())
            LogMe.log(CustomerKYC.get(order.value.name)!!.rateDifference)
            LogMe.log("${SingleAttributedData.getFinalRateInt() + SingleAttributedData.getBufferRateInt() + CustomerKYC.get(order.value.name)!!.rateDifference.toInt()}")
            rateElement.text = "${CustomerData.getCustomerDefaultRate(order.value.name)}"

            rateElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order, entry)
            }

            pcElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order, entry)
            }

            kgElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order, entry)
            }

            paidElement.doOnTextChanged { text, start, before, count ->
                updateEntry(order, entry)
            }

            balanceElement.setOnClickListener {
                if(moreDetailsContainer.visibility == View.VISIBLE) {
                    moreDetailsContainer.visibility = View.GONE
                } else {
                    moreDetailsContainer.visibility = View.VISIBLE
                }
                updateDetailedInfo(order, entry)
            }

            val recordContainer = entry.findViewById<ConstraintLayout>(R.id.one_shot_delivery_fragment_record_container)
            var cardColor = ContextCompat.getColor(this, R.color.one_shot_delivery_odd_card_color)
            if(entrynumber % 2 == 0)
            {
                cardColor = ContextCompat.getColor(this, R.color.one_shot_delivery_even_card_color)
            }
            entrynumber++
            recordContainer.setBackgroundColor(cardColor)

            listContainer.addView(entry)
            updateEntry(order, entry)
            uiMaps[order.value.name] = entry
        }
    }

    private fun updateRates() {
        uiMaps.forEach {
            val rate = CustomerData.getCustomerDefaultRate(it.key)
            val rateElement = it.value.findViewById<TextView>(R.id.one_shot_delivery_fragment_rate)
            rateElement.text = rate.toString()
        }
    }

    private fun updateEntry(order: Map.Entry<String, DeliverCustomerOrders>, entry: View) {
        order.value.deliveredKg = getKgForEntry(entry).toString()
        order.value.deliveredPc = getPcForEntry(entry).toString()
        order.value.todaysAmount = getTodaysSaleAmountForEntry(entry).toString()
        order.value.paid = getPaidAmountForEntry(entry).toString()
        order.value.rate = getRateForEntry(entry).toString()
        order.value.totalDue = "${NumberUtils.getIntOrZero(order.value.prevDue) + getTodaysSaleAmountForEntry(entry)}"
        order.value.balanceDue = "${NumberUtils.getIntOrZero(order.value.prevDue) + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)}"

        val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)

        balanceElement.text = getDueBalance(order.value, entry).toString()
        updateTotals()
        updateDetailedInfo(order, entry)
    }

    fun updateHiddenData() {
        val profitViewContainer = findViewById<LinearLayout>(R.id.osd_profit_details_container)
        if(profitViewContainer.visibility == View.VISIBLE) {
            val profitElement = findViewById<TextView>(R.id.osd_profit)
            val totalDueElement = findViewById<TextView>(R.id.osd_total_due)

            val p = DaySummary.getDayProfit()
            profitElement.text = p.toString()
            totalDueElement.text = "Something"
        }
        updateTotalDueBalance()
    }

    fun updateTotalDueBalance() {
        var sum = 0
        CustomerData.getAllLatestRecords().forEach {
            sum += NumberUtils.getIntOrZero(it.balanceDue)
        }
        val totalDueElement = findViewById<TextView>(R.id.osd_total_due)
        totalDueElement.text = DaySummary.getTotalDueBalance(this).toString()
    }

    private fun updateDetailedInfo(order: Map.Entry<String, DeliverCustomerOrders>, entry: View) {
        val container = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)

        if(container.visibility == View.VISIBLE) {
            val prevDue = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_prev_due)
            val kg = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_kg)
            val rate = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_rate)
            val todaysSale = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_sale_total)
            val total = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_total_due)
            val paid = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_paid_amount)
            val balanceDue = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_balance_due)

            prevDue.text = "₹ ${order.value.prevDue}"
            kg.text = "${order.value.deliveredKg} kg"
            rate.text = "₹ ${order.value.rate}"
            todaysSale.text = "₹ ${order.value.todaysAmount}"
            total.text = "₹ ${order.value.totalDue}"
            paid.text = "₹ ${order.value.paid}"
            balanceDue.text = "₹ ${order.value.balanceDue}"
        }
    }

    private fun getRateForEntry(entry: View): Int {
        val rate = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_rate).text.toString()
        if(rate.isEmpty())
            return 0
        return rate.toInt()
    }

    private fun getPcForEntry(entry: View): Int {
        val pc = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc).text.toString()
        if(pc.isEmpty())
            return 0
        return pc.toInt()

    }

    private fun getKgForEntry(entry: View): Double {
        val kg = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg).text.toString()
        if(kg.isEmpty())
            return 0.0
        return kg.toDouble()

    }

    private fun getPaidAmountForEntry(entry: View): Int {
        val paid = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid).text.toString()
        if(paid.isEmpty())
            return 0
        return paid.toInt()
    }

    private fun getTodaysSaleAmountForEntry(entry: View): Int {
        val kg = getKgForEntry(entry)
        val rate= getRateForEntry(entry)
        return (kg*rate).toInt()
    }

    private fun getDueBalance(order: DeliverCustomerOrders, entry: View): Int {
        val prevBal = order.prevDue
        val bal = NumberUtils.getIntOrZero(prevBal) + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)
        return bal
    }

    private fun getPrevDueBalance(order: DeliverCustomerOrders): Int {
        if(order.prevDue.isEmpty()) {
            return CustomerData.getLastDue(order.name).toInt()
        }
        return order.prevDue.toInt()
    }

    fun updateSingleAttributedDataOnUI() {
        LogMe.log("Updating single attributed data")
        val loadedPc = findViewById<TextView>(R.id.one_shot_delivery_pc)
        val loadedKg = findViewById<TextView>(R.id.one_shot_delivery_kg)
        loadedPc.setText(SingleAttributedData.getRecords().actualLoadPc)
        loadedKg.setText(SingleAttributedData.getRecords().actualLoadKg)

        if(AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_FARM_RATE)) {
            val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
            loadPriceElement.setText(SingleAttributedData.getRecords().finalFarmRate)
        } else {
            findViewById<TextInputLayout>(R.id.osd_farm_rate_container).visibility = View.GONE
        }

        if(AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_BUFFER_RATE)) {
            val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)
            loadBufferElement.setText(SingleAttributedData.getRecords().bufferRate)
        } else {
            findViewById<TextInputLayout>(R.id.osd_buffer_price_container).visibility = View.GONE
        }
    }

    fun getTodaysUpdatedDueMap(): MutableMap<String, Int> {
        var currentDueMapAfterDelivery: MutableMap<String, Int> = mutableMapOf()

        deliveryMapOrderedCustomers.forEach {
            currentDueMapAfterDelivery[it.key] = NumberUtils.getIntOrZero(it.value.balanceDue)
        }

        deliveryMapUnOrderedCustomers.forEach {
            currentDueMapAfterDelivery[it.key] = NumberUtils.getIntOrZero(it.value.balanceDue)
        }
        return currentDueMapAfterDelivery
    }

    fun updateTotals() {
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
            if(NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                sumBalanceDue += NumberUtils.getIntOrZero(it.value.balanceDue)
            }
        }

        deliveryMapUnOrderedCustomers.forEach {
                sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
                sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
                sumSale += NumberUtils.getIntOrZero(it.value.todaysAmount)
                sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
            if(NumberUtils.getIntOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
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
        gatherSingleAttributedData()
        gatherFuelData()
        saveSingleAttributeData()
        deleteDeliveryDataOnServer()
        saveDeliveryData()
    }

    fun gatherSingleAttributedData() {
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
        SingleAttributedData.save(SingleAttributedData.getRecords())
    }

    private fun updateRefuelingUIDetails() {
        val mileageLabel = findViewById<TextView>(R.id.one_shot_delivery_refueling_mileage)
        val refuelingKmDiffLabel = findViewById<TextView>(R.id.one_shot_delivery_refueling_km_diff)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)
        val refuelingDetailsContainer = findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_details_container)
        val refuelingKmContainer = findViewById<TextInputLayout>(R.id.one_shot_delivery_refueling_km_container)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)

        refuelingDetailsContainer.visibility = if(didRefuelElement.isChecked) View.VISIBLE else View.GONE
        didTankFullElement.visibility = if(didRefuelElement.isChecked) View.VISIBLE else View.GONE
        refuelingKmContainer.visibility = if(didTankFullElement.isChecked) View.VISIBLE else View.GONE

        refuelingKmDiffLabel.text = if(didTankFullElement.isChecked) Refueling.getKmDifferenceForRefueling(refuelingKmElement.text.toString()).toString() else "N/A"
        mileageLabel.text = if(didTankFullElement.isChecked) getMileage() + " km/L" else "N/A"

        LogMe.log("KM: " + refuelingKmElement.text.toString())
        LogMe.log("Mileage: " + getMileage())
    }

    private fun getMileage(): String {
        val refuelingQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)

        val refuelingKM = refuelingKmElement.text.toString()
        val refuelingQty = refuelingQtyElement.text.toString()
        LogMe.log("Converting String: " + Refueling.getMileage(refuelingKM, refuelingQty))
        return if(NumberUtils.getDoubleOrZero(refuelingQty) > 0.0)
            Refueling.getMileage(refuelingKM, refuelingQty)
//            "%.3f".format(Refueling.getMileage(refuelingKM, refuelingQty))
        else
            "N/A"
    }

    private fun gatherFuelData() {
        val obj = SingleAttributedData.getRecords()
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        obj.refueling_km = ""
        obj.refueling_prevKm = ""
        obj.refuel_mileage = ""
        obj.refueling_isFullTank = ""
        obj.refueling_amount = ""
        obj.refueling_qty = ""
        obj.refueling_amount = ""
        obj.did_refueled = didRefuelElement.isChecked.toString()

        if(didRefuelElement.isChecked) {
            val refuelQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
            val refuelAmountElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_amount)
            val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
            val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)

            obj.did_refueled = didRefuelElement.isChecked.toString()
            obj.refueling_isFullTank = didTankFullElement.isChecked.toString()
            obj.refueling_qty = refuelQtyElement.text.toString()
            obj.refueling_amount = refuelAmountElement.text.toString()

            if(didTankFullElement.isChecked) {
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

    fun deleteDeliveryDataOnServer() {
        Delete.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.DB_SHEET_ID)
            .tabName(DeliverOrdersConfig.SHEET_INDIVIDUAL_ORDERS_TAB_NAME)
            .build().execute()
    }

    private fun saveDeliveryData() {
        deliveryMapOrderedCustomers.forEach {
            LogMe.log(it.value.name + ":: deliveredKg:" + it.value.deliveredKg)
            LogMe.log(it.value.name + ":: paid:" + it.value.paid)
            if(NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                it.value.deliveryStatus = "DELIVERED"
                DeliverCustomerOrders.save(it.value)
            }
        }
        deliveryMapUnOrderedCustomers.forEach {
            if(NumberUtils.getDoubleOrZero(it.value.deliveredKg) > 0.0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
                it.value.deliveryStatus = "DELIVERED"
                DeliverCustomerOrders.save(it.value)
            }
        }
    }

    fun onClickToggleProfitViewUI(view: View) {
        val profitViewContainer = findViewById<LinearLayout>(R.id.osd_profit_details_container)
        profitViewContainer.visibility = if(profitViewContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        updateHiddenData()
    }
}