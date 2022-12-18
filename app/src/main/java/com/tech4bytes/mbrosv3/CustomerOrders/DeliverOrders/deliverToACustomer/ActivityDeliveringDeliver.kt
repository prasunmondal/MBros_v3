package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class ActivityDeliveringDeliver : AppCompatActivity() {

    lateinit var mainView: View
    lateinit var record: DeliverCustomerOrders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_deliver)
        AppContexts.set(this, this)
        mainView = findViewById(R.id.activity_delivering_deliver_main)

        val inputName = intent.extras!!.get("name") as String
        LogMe.log("Delivering to: $inputName")
        record = getRecord(inputName)
        initiallizeUI()
    }

    fun initiallizeUI() {
        // Get UI Elements
        val nameElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)
        val orderedPcElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::orderedPc)
        val orderedKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::orderedKg)
        val deliveredPcElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredPc)
        val deliveredKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredKg)
        val todaysAmountElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::todaysAmount)
        val prevDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::prevDue)
        val totalDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::totalDue)
        val paidElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::paid)
        val balanceDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::balanceDue)

        // Set UI Values
        UIUtils.setUIElementValue(this, nameElement, record.name)
        UIUtils.setUIElementValue(this, orderedPcElement, record.orderedPc)
        UIUtils.setUIElementValue(this, orderedKgElement, record.orderedKg)
        UIUtils.setUIElementValue(this, deliveredPcElement, record.deliveredPc)
        UIUtils.setUIElementValue(this, deliveredKgElement, record.deliveredKg)
        UIUtils.setUIElementValue(this, todaysAmountElement, record.todaysAmount)
        UIUtils.setUIElementValue(this, prevDueElement, record.prevDue)
        UIUtils.setUIElementValue(this, totalDueElement, record.totalDue)
        UIUtils.setUIElementValue(this, paidElement, record.paid)
        UIUtils.setUIElementValue(this, balanceDueElement, record.balanceDue)

        // Add Listeners
        (deliveredKgElement as AppCompatEditText).doOnTextChanged { text, start, before, count ->
            reCalculateNUpdateValues()
        }
        (paidElement as AppCompatEditText).doOnTextChanged { text, start, before, count -> reCalculateNUpdateValues() }
    }

    private fun reCalculateNUpdateValues() {
        LogMe.log("Re-calculating...")
        val deliveredKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredKg)
        val todaysAmountElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::todaysAmount)
        val totalDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::totalDue)
        val paidElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::paid)
        val balanceDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::balanceDue)

        val calcDeliveredKg = UIUtils.getUIElementValue(deliveredKgElement)
        val paidStr = UIUtils.getUIElementValue(paidElement)

        // Get Today's Amount
        var calcTodaysAmount = 0.0
        if(calcDeliveredKg.isNotEmpty() && calcDeliveredKg.toDouble() > 0) {
            calcTodaysAmount = calcDeliveredKg.toDouble() * record.rate.toDouble()
        }

        // Get Total Amount
        var calcPrevDue = 0.0
        if(record.prevDue.isNotEmpty()) {
            calcPrevDue = record.prevDue.toDouble()
        }
        val calcTotalAmount = calcPrevDue + calcTodaysAmount


        // Get Balance Due Amount
        var calcPaid = 0.0
        if(paidStr.isNotEmpty()) {
            calcPaid = paidStr.toDouble()
        }
        val calcBalanceDue = calcTotalAmount - calcPaid

        UIUtils.setUIElementValue(this, todaysAmountElement, "$calcTodaysAmount")
        UIUtils.setUIElementValue(this, totalDueElement, "$calcTotalAmount")
        UIUtils.setUIElementValue(this, balanceDueElement, "$calcBalanceDue")
    }


    fun getRecord(inputName: String): DeliverCustomerOrders {
        var deliveryObj: DeliverCustomerOrders? = getDataFromDeliveringRecords(inputName)
        if(deliveryObj != null) {
            return deliveryObj
        }

        val orderObj: GetCustomerOrders? = getDataFromOrderRecords(inputName)
        if(orderObj != null) {
            deliveryObj = DeliverCustomerOrders("",
                "",
                orderObj.name,
                orderObj.estimatePc,
                orderObj.estimateKg,
                "",
                "",
                orderObj.rate,
                "",
                orderObj.due,
                "",
                "",
                "",
                "DELIVERING")

            return deliveryObj
        }
        LogMe.log("We didn't find the record in delivering cache or orders placed")
        return null!!
    }

    fun getDataFromDeliveringRecords(inputName: String): DeliverCustomerOrders? {
        DeliverCustomerOrders.get().forEach {
            if(it.name == inputName) {
                return it
            }
        }
        return null
    }

    fun getDataFromOrderRecords(inputName: String): GetCustomerOrders? {
        GetCustomerOrders.get().forEach {
            if(it.name == inputName) {
                return it
            }
        }
        return null
    }

    fun onClickSubmitDeliveredRecord(view: View) {
        record.id = System.currentTimeMillis().toString()
        record.timestamp = DateUtils.getCurrentTimestamp()

    }
}