package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.ActivityDeliveringDeliveryComplete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.UserRoleUtils
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCUtils
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrdersUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties

class DeliverToCustomerActivity : AppCompatActivity() {

    lateinit var mainView: View
    lateinit var record: DeliverToCustomerDataModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_deliver)
        AppContexts.set(this, this)
        AppUtils.logError()
        mainView = findViewById(R.id.activity_delivering_deliver_main)

        val inputName = intent.extras!!.get("name") as String
        LogMe.log("Delivering to: $inputName")
        record = getDeliveryRecord(inputName)!!
        if (record.prevDue.isEmpty()) {
            record.prevDue = CustomerDataUtils.getLastDue(record.name)
        }
        initiallizeUI()
    }

    private fun initiallizeUI() {
        // Get UI Elements
        val nameElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::name)!!
        val orderedPcElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::orderedPc)!!
        val orderedKgElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::orderedKg)!!
        val deliveredPcElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::deliveredPc)!!
        val deliveredKgElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::deliveredKg)!!
        val rate = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::rate)!!
        val todaysAmountElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::todaysAmount)!!
        val prevDueElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::prevDue)!!
        val totalDueElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::totalDue)!!
        val paidElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::paid)!!
        val balanceDueElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::balanceDue)!!
        val bengaliNameElement = findViewById<TextView>(R.id.activity_delivering_deliver_bengali_name)

        // Set UI Values
        UIUtils.setUIElementValue(nameElement, record.name)
        UIUtils.setUIElementValue(orderedPcElement, record.orderedPc.ifEmpty { "--" })
        UIUtils.setUIElementValue(orderedKgElement, record.orderedKg.ifEmpty { "--" })
        UIUtils.setUIElementValue(deliveredPcElement, record.deliveredPc)
        UIUtils.setUIElementValue(deliveredKgElement, record.deliveredKg)
        UIUtils.setUIElementValue(totalDueElement, record.totalDue)
        UIUtils.setUIElementValue(paidElement, record.paid)
        UIUtils.setUIElementValue(balanceDueElement, CustomerDataUtils.getLastDue(record.name))
        UIUtils.setUIElementValue(bengaliNameElement, CustomerKYCUtils.getCustomerByEngName(record.name)!!.nameBeng)

        if (UserRoleUtils.doesHaveRole(ActivityAuthEnums.ADMIN)) {
            UIUtils.setUIElementValue(rate, record.rate)
            UIUtils.setUIElementValue(todaysAmountElement, record.todaysAmount)
        }

        if (record.rate.isEmpty() && (UserRoleUtils.doesHaveRole(ActivityAuthEnums.ADMIN) || UserRoleUtils.doesHaveRole(ActivityAuthEnums.SHOW_RATES_IN_DELIVERY_PAGE))) {
            UIUtils.setUIElementValue(rate, ("0${SingleAttributedData.getRecords().finalFarmRate}".toInt() + "0${SingleAttributedData.getRecords().bufferRate}".toInt() + CustomerKYCUtils.getCustomerByEngName(record.name)!!.rateDifference.toInt()).toString())
            (rate as EditText).setTextColor(ContextCompat.getColor(this, R.color.red))
        }

        if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_PROFITS) || CustomerKYCUtils.showBalance(UIUtils.getUIElementValue(nameElement))) {
            UIUtils.setUIElementValue(prevDueElement, record.prevDue)
        }

        // Don't show Today's Amount for privacy reasons
        // UIUtils.setUIElementValue(todaysAmountElement, record.todaysAmount)

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
            if (getDeliveredRate() == 0.0 && getDeliveredKg() == 0.0 &&
                getDeliveredPc() == 0 && getPaidAmountText().isEmpty()
            ) {
                false
            } else {
                validateSellingData()
            }

        setValidityColors(deliveredRateContainer, isValid)
        setValidityColors(deliveredPcContainer, isValid)
        setValidityColors(deliveredKgContainer, isValid)

        val nameElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::name)!!
        if (!AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_PROFITS) && !CustomerKYCUtils.showBalance(UIUtils.getUIElementValue(nameElement))) {
            setValidityColors(deliveredRateContainer, true)
        }
    }

    private fun getDeliveredRate(): Double {
        val deliveredRateElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::rate)!!
        return NumberUtils.getDoubleOrZero(UIUtils.getUIElementValue(deliveredRateElement))
    }

    private fun getDeliveredKg(): Double {
        val deliveredKgElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::deliveredKg)!!
        return NumberUtils.getDoubleOrZero(UIUtils.getUIElementValue(deliveredKgElement))
    }

    private fun getDeliveredPc(): Int {
        val deliveredPcElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::deliveredPc)!!
        return NumberUtils.getIntOrZero(UIUtils.getUIElementValue(deliveredPcElement))
    }

    private fun getPaidAmountText(): String {
        val deliveredPaidElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::paid)!!
        return UIUtils.getUIElementValue(deliveredPaidElement)
    }

    private fun validateSellingData(): Boolean {
        val nameElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::name)!!

        if (!CustomerKYCUtils.showBalance(UIUtils.getUIElementValue(nameElement))) {
            if (getDeliveredKg() == 0.0 && getDeliveredPc() == 0)
                return true

            if (getDeliveredKg() == 0.0 || getDeliveredPc() == 0)
                return false

            return true
        }

        if (getDeliveredKg() == 0.0 && getDeliveredPc() == 0 && getDeliveredRate() == 0.0)
            return true

        if (getDeliveredKg() == 0.0 || getDeliveredPc() == 0 || getDeliveredRate() == 0.0)
            return false

        return true
    }

    private fun validatePaid() {
        val nameElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::name)!!
        val deliveredPaidContainer = findViewById<LinearLayout>(R.id.activity_delivering_deliver_delivering_paid_container)

        if (!CustomerKYCUtils.showBalance(UIUtils.getUIElementValue(nameElement))) {
            setValidityColors(deliveredPaidContainer, true)
            return
        }

        if (getDeliveredRate() == 0.0 && getDeliveredKg() == 0.0 && getDeliveredPc() == 0 && getPaidAmountText().isEmpty()) {
            setValidityColors(deliveredPaidContainer, false)
            return
        }

        setValidityColors(deliveredPaidContainer, getPaidAmountText().isNotEmpty())
    }

    private fun setValidityColors(element: View, isValid: Boolean) {
        if (isValid) {
            element.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_valid))
        } else {
            element.setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
        }
    }

    private fun reCalculateNUpdateValues() {
        val name = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::name)!!

        val todaysAmountElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::todaysAmount)!!
        val totalDueElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::totalDue)!!
        val balanceDueElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::balanceDue)!!

        // do not update UI if show balances is false
        if (!AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_PROFITS) && !CustomerKYCUtils.showBalance(UIUtils.getUIElementValue(name))) {
            UIUtils.setUIElementValue(todaysAmountElement, "0.00")
            UIUtils.setUIElementValue(totalDueElement, "0.00")
            UIUtils.setUIElementValue(balanceDueElement, "0.00")
            return
        }

        UIUtils.setUIElementValue(todaysAmountElement, "${calculateTodaysAmount()}")
        UIUtils.setUIElementValue(totalDueElement, "${calculateTotalAmount()}")
        UIUtils.setUIElementValue(balanceDueElement, "${calculateBalanceDue()}")
    }

    private fun calculateBalanceDue(): Double {
        val paidElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::paid)!!
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
        val deliveredKgElement = getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::deliveredKg)!!
        val calcDeliveredKg = UIUtils.getUIElementValue(deliveredKgElement)
        var calcTodaysAmount = 0.0
        if (calcDeliveredKg.isNotEmpty() && calcDeliveredKg.toDouble() > 0) {
            calcTodaysAmount = calcDeliveredKg.toDouble() * NumberUtils.getIntOrZero(UIUtils.getUIElementValue(getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::rate)!!))
        }
        return calcTodaysAmount.toInt()
    }

    fun onClickSubmitDeliveredRecord(view: View) {
        val rate = UIUtils.getUIElementValue(getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::rate)!!)
        val deliveredWeight = UIUtils.getUIElementValue(getUiElementFromDeliveringPage(mainView, DeliverToCustomerDataModel::deliveredKg)!!)
        if (NumberUtils.getDoubleOrZero(rate) == 0.0 && NumberUtils.getDoubleOrZero(deliveredWeight) != 0.0) {
            Toast.makeText(this, "সব গুলো লেখা হয়নি", Toast.LENGTH_LONG).show()
            return
        }

        getAllAttributesOfClass<DeliverToCustomerDataModel>().forEach { kMutableProperty ->
            val uiElement = getUiElementFromDeliveringPage(mainView, kMutableProperty)
            if (uiElement != null) {
                ReflectionUtils.setAttribute(record, kMutableProperty, UIUtils.getUIElementValue(uiElement))
            }
        }
        record.id = System.currentTimeMillis().toString()
        record.timestamp = DateUtils.getCurrentTimestamp()
        record.deliveryStatus = "DELIVERED"
        record.todaysAmount = "${calculateTodaysAmount()}"
        record.totalDue = "${calculateTotalAmount()}"
        record.balanceDue = "${calculateBalanceDue()}"

        val saveBtnElement = findViewById<Button>(R.id.activity_delivering_deliver_submit_btn)
        Thread {
            runOnUiThread {
                saveBtnElement.isEnabled = false
                saveBtnElement.alpha = .5f
                saveBtnElement.isClickable = false
                saveBtnElement.text = "Saving..."
            }
            DeliverToCustomerDataHandler.save(record, true)
            runOnUiThread {
                saveBtnElement.isEnabled = true
                saveBtnElement.alpha = 1.0f
                saveBtnElement.isClickable = true
                saveBtnElement.text = "Save"
            }
            goToActivityDeliveringDeliveryComplete()
        }.start()
    }

    private fun goToActivityDeliveringDeliveryComplete() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliveryComplete::class.java)
        startActivity(switchActivityIntent)
        finishAffinity()
    }

    fun <T> getAllAttributesOfClass(): ArrayList<KMutableProperty1<T, String>> {
        val list: ArrayList<KMutableProperty1<T, String>> = arrayListOf()
        (DeliverToCustomerDataModel::class).declaredMemberProperties.forEach {
            list.add(it as KMutableProperty1<T, String>)
        }
        return list
    }


    companion object {

        fun getUiElementFromDeliveringPage(view: View, attribute: KMutableProperty1<DeliverToCustomerDataModel, *>): View? {
            return when (attribute) {
                DeliverToCustomerDataModel::name -> view.findViewById<TextView>(R.id.activity_delivering_deliver_name)
                DeliverToCustomerDataModel::rate -> view.findViewById<TextView>(R.id.activity_delivering_deliver_rate)
                DeliverToCustomerDataModel::orderedPc -> view.findViewById<TextView>(R.id.activity_delivering_deliver_ordered_pc)
                DeliverToCustomerDataModel::orderedKg -> view.findViewById<TextView>(R.id.activity_delivering_deliver_ordered_kg)
                DeliverToCustomerDataModel::deliveredPc -> view.findViewById<TextView>(R.id.activity_delivering_deliver_delivering_pc)
                DeliverToCustomerDataModel::deliveredKg -> view.findViewById<TextView>(R.id.activity_delivering_deliver_delivering_kg)
                DeliverToCustomerDataModel::todaysAmount -> view.findViewById<TextView>(R.id.activity_delivering_deliver_todays_amount)
                DeliverToCustomerDataModel::prevDue -> view.findViewById<TextView>(R.id.activity_delivering_deliver_prev_due)
                DeliverToCustomerDataModel::totalDue -> view.findViewById<TextView>(R.id.activity_delivering_deliver_all_total)
                DeliverToCustomerDataModel::paid -> view.findViewById<TextView>(R.id.activity_delivering_deliver_paid)
                DeliverToCustomerDataModel::balanceDue -> view.findViewById<TextView>(R.id.activity_delivering_deliver_balance_due)
                else -> null
            }
        }

        fun getDeliveryRecord(inputName: String): DeliverToCustomerDataModel? {
            var deliveryObj: DeliverToCustomerDataModel? = DeliverToCustomerCalculations.getByName(inputName)
            if (deliveryObj != null) {
                return deliveryObj
            }

            val orderObj: GetCustomerOrders? = GetCustomerOrdersUtils.getByName(inputName)
            if (orderObj != null) {
                deliveryObj = DeliverToCustomerDataModel(
                    id = "${System.currentTimeMillis()}",
                    timestamp = DateUtils.getCurrentTimestamp(),
                    name = orderObj.name,
                    orderedPc = orderObj.orderedPc,
                    orderedKg = orderObj.orderedKg,
                    rate = orderObj.rate,
                    prevDue = orderObj.prevDue,
                    deliveryStatus = "DELIVERING"
                )

                return deliveryObj
            }
            LogMe.log("We didn't find the record in delivering cache or orders placed")
            return null
        }
    }
}