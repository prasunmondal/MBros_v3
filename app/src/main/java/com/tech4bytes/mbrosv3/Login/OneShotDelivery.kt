package com.tech4bytes.mbrosv3.Login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class OneShotDelivery : AppCompatActivity() {

    var deliveryMapOrderedCustomers: MutableMap<String, DeliverCustomerOrders> = mutableMapOf()
    var deliveryMapUnOrderedCustomers: MutableMap<String, DeliverCustomerOrders> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this)
        AppUtils.logError()

        populateDeliveryMap()
        showOrders()
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

    fun showOrders(listOfCustomers: MutableMap<String, DeliverCustomerOrders>, container: Int) {

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

            listContainer.addView(entry)
        }
    }

    private fun updateEntry(order: Map.Entry<String, DeliverCustomerOrders>, entry: View) {
        order.value.deliveredKg = getKgForEntry(entry).toString()
        order.value.deliveredPc = getPcForEntry(entry).toString()
        order.value.todaysAmount = getTodaysSaleAmountForEntry(entry).toString()
        order.value.paid = getPaidAmountForEntry(entry).toString()
        order.value.rate = getRateForEntry(entry).toString()

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
        val prevBal = getPrevDueBalance(order)
        val bal = prevBal + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)
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
        showOrders()
    }

    fun updateTotals() {
        val totalPcElement = findViewById<TextView>(R.id.one_shot_delivery_total_pc)
        val totalKgElement = findViewById<TextView>(R.id.one_shot_delivery_total_kg)
        val totalSaleElement = findViewById<TextView>(R.id.one_shot_delivery_total_sale)
        val totalShortageElement = findViewById<TextView>(R.id.one_shot_delivery_total_shortage)
        val totalCollectedElement = findViewById<TextView>(R.id.one_shot_delivery_total_collected_amount)

        var sumPc = 0
        var sumKg = 0.0
        var sumSale = 0
        var sumAmountCollected = 0

        deliveryMapOrderedCustomers.forEach {
            sumPc += NumberUtils.getIntOrZero(it.value.deliveredPc)
            sumKg += NumberUtils.getDoubleOrZero(it.value.deliveredKg)
            sumSale += NumberUtils.getIntOrZero(it.value.todaysAmount)
            sumAmountCollected += NumberUtils.getIntOrZero(it.value.paid)
        }

        totalPcElement.text = "pc: $sumPc"
        totalKgElement.text = "$sumKg kg"
        totalSaleElement.text = "Sale: Rs $sumSale"
        totalShortageElement.text = "0"
        totalCollectedElement.text = "Collection: Rs: $sumAmountCollected"
    }

}