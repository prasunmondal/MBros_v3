package com.tech4bytes.mbrosv3.Login

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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.ActivityDeliveringDeliver
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling

class OneShotDelivery : AppCompatActivity() {

    var deliveryMapOrderedCustomers: MutableMap<String, DeliverCustomerOrders> = mutableMapOf()
    var deliveryMapUnOrderedCustomers: MutableMap<String, DeliverCustomerOrders> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this)
        AppUtils.logError()
        window.addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        populateDeliveryMap()
        updateSingleAttributedDataOnUI()
        showOrders()
        initiallizeUI()
    }

    private fun initiallizeUI() {
        val didRefuelElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val refuelingDetailsContainer = findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_details_container)
        val refuelingKmContainer = findViewById<TextInputLayout>(R.id.one_shot_delivery_refueling_km_container)

        refuelingDetailsContainer.visibility = if(didRefuelElement.isChecked) View.VISIBLE else View.GONE
        didTankFullElement.visibility = if(didRefuelElement.isChecked) View.VISIBLE else View.GONE
        refuelingKmContainer.visibility = if(didTankFullElement.isChecked) View.VISIBLE else View.GONE

        didRefuelElement.setOnCheckedChangeListener { _, isChecked ->
            initiallizeUI()
        }

        didTankFullElement.setOnCheckedChangeListener { _, isChecked ->
            initiallizeUI()
        }
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
            rateElement.text = "${SingleAttributedData.getFinalRateInt() + SingleAttributedData.getBufferRateInt() + CustomerKYC.get(order.value.name)!!.rateDifference.toInt()}"

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

    fun onClickSyncInputsOffline(view: View) {
        val record = SingleAttributedData.getRecords()
        val loadPcElement = findViewById<EditText>(R.id.one_shot_delivery_pc)
        val loadKgElement = findViewById<EditText>(R.id.one_shot_delivery_kg)
        val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
        val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)

        record.actualLoadPc = loadPcElement.text.toString()
        record.actualLoadKg = loadKgElement.text.toString()
        record.finalFarmRate = loadPriceElement.text.toString()
        record.bufferRate = loadBufferElement.text.toString()
        SingleAttributedData.saveToLocal(record)
        updateSingleAttributedDataOnUI()
        showOrders()
    }

    fun updateSingleAttributedDataOnUI() {
        LogMe.log("Updating single attributed data")
        val loadedPc = findViewById<TextView>(R.id.one_shot_delivery_pc)
        val loadedKg = findViewById<TextView>(R.id.one_shot_delivery_kg)
        loadedPc.setText(SingleAttributedData.getRecords().actualLoadPc)
        loadedKg.setText(SingleAttributedData.getRecords().actualLoadKg)

        if(RolesUtils.doesHaveRole(Roles.SHOW_RATES_IN_DELIVERY_PAGE)) {
            val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
            val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)

            loadPriceElement.setText(SingleAttributedData.getRecords().finalFarmRate)
            loadBufferElement.setText(SingleAttributedData.getRecords().bufferRate)
        }
    }

    fun updateTotals() {
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
            if(NumberUtils.getIntOrZero(it.value.deliveredKg) > 0 || NumberUtils.getIntOrZero(it.value.paid) > 0) {
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
        totalSaleElement.text = "＄ ₹ $sumSale"

        totalShortageElement.text = "▼ ${"%.3f".format(shortage)} kg"
        totalCollectedElement.text = "\uD83D\uDCB0 ₹ $sumAmountCollected"
        totalBalanceDueElement.text = "\uD83D\uDCB8 $sumBalanceDue"
    }

    fun onClickSaveOneShotDeliveryDataBtn(view: View) {
        gatherSingleAttributedData()
        gatherFuelData()
        saveSingleAttributeData()
//        saveDeliveryData()
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

    private fun gatherFuelData() {
        var obj = SingleAttributedData.getRecords()

        val isRefueledElement = findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        if(isRefueledElement.isChecked) {
            val isRefueledToFullTank = findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
            val refuelQtyElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
            val refuelAmountElement = findViewById<EditText>(R.id.one_shot_delivery_fuel_amount)

            obj.refueling_qty = refuelQtyElement.text.toString()
            obj.refueling_amount = refuelAmountElement.text.toString()

            if(isRefueledToFullTank.isChecked) {
                val refuelingKmElement = findViewById<EditText>(R.id.one_shot_delivery_refueling_km)
                obj.refueling_km = refuelingKmElement.text.toString()
            } else {
                obj.refueling_km = ""
            }
        }

        SingleAttributedData.saveToLocal(obj)
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

}