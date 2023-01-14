package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.ActivityDeliveringDeliveryComplete
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Login.RolesModel
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

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
        val nameElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)!!
        val orderedPcElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::orderedPc)!!
        val orderedKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::orderedKg)!!
        val deliveredPcElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredPc)!!
        val deliveredKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredKg)!!
        val rate = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::rate)!!
        val todaysAmountElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::todaysAmount)!!
        val prevDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::prevDue)!!
        val totalDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::totalDue)!!
        val paidElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::paid)!!
        val balanceDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::balanceDue)!!

        var orderedPcText =

        // Set UI Values
        UIUtils.setUIElementValue(this, nameElement, record.name)
        UIUtils.setUIElementValue(this, orderedPcElement, record.orderedPc.ifEmpty { "--" })
        UIUtils.setUIElementValue(this, orderedKgElement, record.orderedPc.ifEmpty { "--" })
        UIUtils.setUIElementValue(this, deliveredPcElement, record.deliveredPc)
        UIUtils.setUIElementValue(this, deliveredKgElement, record.deliveredKg)
        UIUtils.setUIElementValue(this, prevDueElement, record.prevDue)
        UIUtils.setUIElementValue(this, totalDueElement, record.totalDue)
        UIUtils.setUIElementValue(this, paidElement, record.paid)
        UIUtils.setUIElementValue(this, balanceDueElement, record.balanceDue)

        // Don't show Today's Amount for privacy reasons
        // UIUtils.setUIElementValue(this, todaysAmountElement, record.todaysAmount)

        // Add Listeners
        (deliveredKgElement as AppCompatEditText).doOnTextChanged { text, start, before, count ->
            reCalculateNUpdateValues()
        }
        (paidElement as AppCompatEditText).doOnTextChanged { text, start, before, count -> reCalculateNUpdateValues() }
        (rate as AppCompatEditText).doOnTextChanged { text, start, before, count -> reCalculateNUpdateValues() }
    }

    private fun reCalculateNUpdateValues() {
        LogMe.log("Re-calculating...")
        val name = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)!!

        // do not update UI if show balances is false
        if(!RolesModel.isEligibleToViewHiddenDue() && !CustomerKYC.showBalance(UIUtils.getUIElementValue(name)))
            return

        val todaysAmountElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::todaysAmount)!!
        val totalDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::totalDue)!!
        val balanceDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::balanceDue)!!

        UIUtils.setUIElementValue(this, todaysAmountElement, "${calculateTodaysAmount()}")
        UIUtils.setUIElementValue(this, totalDueElement, "${calculateTotalAmount()}")
        UIUtils.setUIElementValue(this, balanceDueElement, "${calculateBalanceDue()}")
    }

    private fun calculateBalanceDue(): Double {
        val paidElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::paid)!!
        val paidStr = UIUtils.getUIElementValue(paidElement)
        var calcPaid = 0.0
        if (paidStr.isNotEmpty()) {
            calcPaid = paidStr.toDouble()
        }
        return calculateTotalAmount() - calcPaid
    }

    private fun calculateTotalAmount(): Double {
        var calcPrevDue = 0.0
        if (record.prevDue.isNotEmpty()) {
            calcPrevDue = record.prevDue.toDouble()
        }
        return calcPrevDue + calculateTodaysAmount()
    }

    private fun calculateTodaysAmount(): Int {
        val deliveredKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredKg)!!
        val calcDeliveredKg = UIUtils.getUIElementValue(deliveredKgElement)
        var calcTodaysAmount = 0.0
        if(calcDeliveredKg.isNotEmpty() && calcDeliveredKg.toDouble() > 0) {
            calcTodaysAmount = calcDeliveredKg.toDouble() * NumberUtils.getIntOrZero(UIUtils.getUIElementValue(DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::rate)!!))
        }
        return calcTodaysAmount.roundToInt()
    }

    fun getRecord(inputName: String): DeliverCustomerOrders {
        var deliveryObj: DeliverCustomerOrders? = DeliverCustomerOrders.getByName(inputName)
        if(deliveryObj != null) {
            return deliveryObj
        }

        val orderObj: GetCustomerOrders? = GetCustomerOrders.getByName(inputName)
        if(orderObj != null) {
            deliveryObj = DeliverCustomerOrders(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = orderObj.name,
                orderedPc = orderObj.estimatePc,
                orderedKg = orderObj.estimateKg,
                rate = orderObj.rate,
                prevDue = orderObj.prevDue,
                deliveryStatus = "DELIVERING")

            return deliveryObj
        }
        LogMe.log("We didn't find the record in delivering cache or orders placed")
        return null!!
    }


    fun onClickSubmitDeliveredRecord(view: View) {
        getAllAttributesOfClass<DeliverCustomerOrders>().forEach { kMutableProperty ->
            val uiElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, kMutableProperty)
            if(uiElement != null) {
                ReflectionUtils.setAttribute(record, kMutableProperty, UIUtils.getUIElementValue(uiElement))
            }
        }
        record.id = System.currentTimeMillis().toString()
        record.timestamp = DateUtils.getCurrentTimestamp()
        record.deliveryStatus = "DELIVERED"
        record.todaysAmount = "${calculateTodaysAmount()}"
        record.totalDue = "${calculateTotalAmount()}"
        record.balanceDue = "${calculateBalanceDue()}"

        DeliverCustomerOrders.save(record)
        goToActivityDeliveringDeliveryComplete()
    }

    private fun goToActivityDeliveringDeliveryComplete() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliveryComplete::class.java)
        startActivity(switchActivityIntent)
    }

    fun <T> getAllAttributesOfClass(): ArrayList<KMutableProperty1<T, String>> {
        val list: ArrayList<KMutableProperty1<T, String>> = arrayListOf()
        (DeliverCustomerOrders::class).declaredMemberProperties.forEach {
            list.add(it as KMutableProperty1<T, String>)
        }
        return list
    }
}