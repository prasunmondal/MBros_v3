package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.ActivityDeliveringDeliveryComplete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Login.Roles
import com.tech4bytes.mbrosv3.Login.RolesModel
import com.tech4bytes.mbrosv3.Login.RolesUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class ActivityDeliveringDeliver : AppCompatActivity() {

    lateinit var mainView: View
    lateinit var record: DeliverCustomerOrders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_deliver)
        AppContexts.set(this, this)
        AppUtils.logError()
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

        // Set UI Values
        UIUtils.setUIElementValue(this, nameElement, record.name)
        UIUtils.setUIElementValue(this, orderedPcElement, record.orderedPc.ifEmpty { "--" })
        UIUtils.setUIElementValue(this, orderedKgElement, record.orderedKg.ifEmpty { "--" })
        UIUtils.setUIElementValue(this, deliveredPcElement, record.deliveredPc)
        UIUtils.setUIElementValue(this, deliveredKgElement, record.deliveredKg)
        UIUtils.setUIElementValue(this, totalDueElement, record.totalDue)
        UIUtils.setUIElementValue(this, paidElement, record.paid)
        UIUtils.setUIElementValue(this, balanceDueElement, CustomerData.getLastDue(record.name))

        if(RolesUtils.doesHaveRole(Roles.ADMIN)) {
            UIUtils.setUIElementValue(this, rate, record.rate)
            UIUtils.setUIElementValue(this, todaysAmountElement, record.todaysAmount)
        }

        if(record.rate.isEmpty() && (RolesUtils.doesHaveRole(Roles.ADMIN) || RolesUtils.doesHaveRole(Roles.SHOW_RATES_IN_DELIVERY_PAGE))) {
            UIUtils.setUIElementValue(this, rate, (SingleAttributedData.getRecords().finalFarmRate.toInt() + CustomerKYC.get(record.name)!!.rateDifference.toInt()).toString())
            (rate as EditText).setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        if (RolesModel.isEligibleToViewHiddenDue() || CustomerKYC.showBalance(UIUtils.getUIElementValue(nameElement))) {
            UIUtils.setUIElementValue(this, prevDueElement, record.prevDue)
        }

        // Don't show Today's Amount for privacy reasons
        // UIUtils.setUIElementValue(this, todaysAmountElement, record.todaysAmount)

        showSellingDataValidation()
        validatePaid()
        reCalculateNUpdateValues()

        // Add Listeners
        (rate as AppCompatEditText).doOnTextChanged { text, start, before, count ->
            validateSellingData()
            showSellingDataValidation()
            reCalculateNUpdateValues()
            (rate as EditText).setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        (deliveredPcElement as AppCompatEditText).doOnTextChanged { text, start, before, count ->
            showSellingDataValidation()
        }
        (deliveredKgElement as AppCompatEditText).doOnTextChanged { text, start, before, count ->
            showSellingDataValidation()
            reCalculateNUpdateValues()
        }
        (paidElement as AppCompatEditText).doOnTextChanged { text, start, before, count ->
            showSellingDataValidation()
            validatePaid()
            reCalculateNUpdateValues()
        }
    }

    private fun showSellingDataValidation() {
        val deliveredRateContainer = findViewById<LinearLayout>(R.id.activity_delivering_deliver_delivering_rate_container)
        val deliveredPcContainer = findViewById<LinearLayout>(R.id.activity_delivering_deliver_delivering_pc_container)
        val deliveredKgContainer = findViewById<LinearLayout>(R.id.activity_delivering_deliver_delivering_kg_container)

        val isValid: Boolean =
            if(getDeliveredRate() == 0.0 && getDeliveredKg() == 0.0 &&
                getDeliveredPc() == 0 && getPaidAmountText().isEmpty()) {
                false
            } else {
                validateSellingData()
            }

        setValidityColors(deliveredRateContainer, isValid)
        setValidityColors(deliveredPcContainer, isValid)
        setValidityColors(deliveredKgContainer, isValid)

        val nameElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)!!
        if (!RolesModel.isEligibleToViewHiddenDue() && !CustomerKYC.showBalance(UIUtils.getUIElementValue(nameElement))) {
            setValidityColors(deliveredRateContainer, true)
        }
    }

    private fun getDeliveredRate(): Double {
        val deliveredRateElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::rate)!!
        val deliveredRateText = NumberUtils.getDoubleOrZero(UIUtils.getUIElementValue(deliveredRateElement))
        return deliveredRateText
    }

    private fun getDeliveredKg(): Double {
        val deliveredKgElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredKg)!!
        val deliveredKgText = NumberUtils.getDoubleOrZero(UIUtils.getUIElementValue(deliveredKgElement))
        return deliveredKgText
    }

    private fun getDeliveredPc(): Int {
        val deliveredPcElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredPc)!!
        val deliveredPcText = NumberUtils.getIntOrZero(UIUtils.getUIElementValue(deliveredPcElement))
        return deliveredPcText
    }

    private fun getPaidAmountText(): String {
        val deliveredPaidElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::paid)!!
        val deliveredPaidText = UIUtils.getUIElementValue(deliveredPaidElement)
        return deliveredPaidText
    }

    private fun validateSellingData(): Boolean {
        val nameElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)!!

        if (!CustomerKYC.showBalance(UIUtils.getUIElementValue(nameElement))) {
            if(getDeliveredKg() == 0.0 && getDeliveredPc() == 0)
                return true

            if(getDeliveredKg() == 0.0 || getDeliveredPc() == 0)
                return false

            return true
        }

        if(getDeliveredKg() == 0.0 && getDeliveredPc() == 0 && getDeliveredRate() == 0.0)
            return true

        if(getDeliveredKg() == 0.0 || getDeliveredPc() == 0 || getDeliveredRate() == 0.0)
            return false

        return true
    }

    private fun validatePaid() {
        val nameElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)!!
        val deliveredPaidContainer = findViewById<LinearLayout>(R.id.activity_delivering_deliver_delivering_paid_container)

        if (!CustomerKYC.showBalance(UIUtils.getUIElementValue(nameElement))) {
            setValidityColors(deliveredPaidContainer, true)
            return
        }

        if(getDeliveredRate() == 0.0 && getDeliveredKg() == 0.0 && getDeliveredPc() == 0 && getPaidAmountText().isEmpty()) {
            setValidityColors(deliveredPaidContainer, false)
            return
        }

        setValidityColors(deliveredPaidContainer, getPaidAmountText().isNotEmpty())
    }

    private fun setValidityColors(element: View, isValid: Boolean) {
        if(isValid) {
            element.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_valid))
        } else {
            element.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
        }
    }

    private fun reCalculateNUpdateValues() {
        LogMe.log("Re-calculating...")
        val name = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::name)!!

        val todaysAmountElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::todaysAmount)!!
        val totalDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::totalDue)!!
        val balanceDueElement = DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::balanceDue)!!

        // do not update UI if show balances is false
        if(!RolesModel.isEligibleToViewHiddenDue() && !CustomerKYC.showBalance(UIUtils.getUIElementValue(name))) {
            UIUtils.setUIElementValue(this, todaysAmountElement, "0.00")
            UIUtils.setUIElementValue(this, totalDueElement, "0.00")
            UIUtils.setUIElementValue(this, balanceDueElement, "0.00")
            return
        }

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
        return calcTodaysAmount.toInt()
    }

    private fun getRecord(inputName: String): DeliverCustomerOrders {
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
        val rate = UIUtils.getUIElementValue(DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::rate)!!)
        val deliveredWeight = UIUtils.getUIElementValue(DeliverCustomerOrders.getUiElementFromDeliveringPage(mainView, DeliverCustomerOrders::deliveredKg)!!)
        if(NumberUtils.getDoubleOrZero(rate) == 0.0 && NumberUtils.getDoubleOrZero(deliveredWeight) != 0.0) {
            Toast.makeText(this, "সব গুলো লেখা হয়নি", Toast.LENGTH_LONG).show()
            return
        }

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
        finishAffinity()
    }

    fun <T> getAllAttributesOfClass(): ArrayList<KMutableProperty1<T, String>> {
        val list: ArrayList<KMutableProperty1<T, String>> = arrayListOf()
        (DeliverCustomerOrders::class).declaredMemberProperties.forEach {
            list.add(it as KMutableProperty1<T, String>)
        }
        return list
    }
}