package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliveringUtils.calculateDeliverAmount
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.SMSOrderModel
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.OneShot.Delivery.OSDDeliveryEntryInfo
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotDelivery
import com.tech4bytes.mbrosv3.OneShot.Delivery.ReferralType
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getDoubleOrZero
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getIntOrBlank
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getIntOrZero

data class DeliverToCustomerDataModel(
    var id: String = "",
    var date: String = "",
    var timestamp: String = "",
    var name: String = "",
    var orderedPc: String = "",
    var orderedKg: String = "",
    var deliveredPc: String = "",
    var deliveredKg: String = "",
    var rate: String = "",
    var deliverAmount: String = "",
    var customerAccount: String = "",
    var prevDue: String = "",
    var khataBalance: String = "",
    var paidCash: String = "",
    var paidOnline: String = "",
    var paid: String = "",
    var totalBalance: String = "",
    var deliveryStatus: String = "",
    var adjustments: String = "",
    var notes: String = "",
    var otherBalances: String = "",
    var adjustmentNotes: String = ""
) : java.io.Serializable {

    override fun toString(): String {
        return "DeliverToCustomerDataModel(id='$id', date='$date', timestamp='$timestamp', name='$name', orderedPc='$orderedPc', orderedKg='$orderedKg', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', todaysAmount='$deliverAmount', customerAccount='$customerAccount', prevDue='$prevDue', totalDue='$khataBalance', paidCash='$paidCash', paidOnline='$paidOnline', paid='$paid', balanceDue='$totalBalance', deliveryStatus='$deliveryStatus', discount='$adjustments', notes='$notes')"
    }

    fun setDeliveredPc(deliveredPc: Int, view: View) {
        this.deliveredPc = deliveredPc.toString()
        calculate(this, view)
    }

    fun setDeliveredKg(deliveredKg: Double, view: View) {
        this.deliveredKg = deliveredKg.toString()
        calculate(this, view)

        // update hint color for pc when kg > 0
        val pcView = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc)
    }

    fun setPaidCash(paidCash: Int, view: View) {
        this.paidCash = paidCash.toString()
        calculate(this, view)
    }

    fun setPaidOnline(paidOnline: Int, view: View?) {
        this.paidOnline = paidOnline.toString()
        calculate(this, view)
    }

    fun setRate(context: Context, rate: Int, view: View) {
        this.rate = rate.toString()
        calculate(this, view)

        // update rate view colors
        val rateElement = view.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
        rateElement.setText(rate.toString())
        val refreshRateButton = view.findViewById<ImageView>(R.id.one_shot_delivery_fragment_refresh_btn)
        if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_RATE_RESET_BUTTON) && getIntOrZero(rateElement.text.toString()) != CustomerDataUtils.getCustomerDefaultRate(this.name)) {
            rateElement.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
            rateElement.setTextColor(ContextCompat.getColor(context, R.color.white))
            refreshRateButton.visibility = View.VISIBLE
        } else {
            rateElement.setBackgroundColor(0x00000000)
            rateElement.setTextColor(rateElement.textColors.defaultColor)
            refreshRateButton.visibility = View.GONE
        }
    }

    fun calculate(deliverObj: DeliverToCustomerDataModel, view: View?) {
        if(view == null)
            calculate(deliverObj.name, view)
        calculate(OSDDeliveryEntryInfo.getName(view), view)
    }

    fun calculate(name: String, view: View?) {
        val nameFromView = name
        val customerProfile = CustomerKYC.getCustomerByEngName(nameFromView)!!
        val deliveredAmount = calculateDeliverAmount(deliveredKg, rate)
        val discounts = 0

        prevDue = CustomerDueData.getLastFinalizedDue(nameFromView)
        deliverAmount = deliveredAmount.toString()
        paid = (getIntOrZero(paidCash) + getIntOrZero(paidOnline)).toString()
        calculateAdjustmentAmount()

        val referredByView = OSDDeliveryEntryInfo.uiMaps[customerProfile.referredBy]
        if (referredByView != null)
            OneShotDelivery.deliverRecords[customerProfile.referredBy]!!.calculate(OneShotDelivery.deliverRecords[customerProfile.referredBy]!!, referredByView)

        adjustments = getIntOrBlank(MoneyAdjustments.getAdjustmentAmount(nameFromView).toString())
        adjustmentNotes = MoneyAdjustments.getAdjustmentMessages(nameFromView)
        khataBalance =
            (getIntOrZero(prevDue) + deliveredAmount + getIntOrZero(adjustments) - discounts - getIntOrZero(paidCash) - getIntOrZero(
                paidOnline
            )).toString()
        otherBalances = CustomerKYC.getByName(nameFromView)!!.otherBalances
        totalBalance = (getIntOrZero(khataBalance) + getIntOrZero(otherBalances)).toString()

        if (view != null) {
            OSDDeliveryEntryInfo.updateDerivedAttributesInUi(view, this)
            OneShotDelivery.updateTotals()
        }
    }

    private fun calculateAdjustmentAmount() {
        LogMe.log("Calculating Adjustment Amount for: " + this.name)
        val customerProfile = CustomerKYC.getCustomerByEngName(this.name)
        val adjustmentWith = customerProfile!!.referredBy
        val adjustmentType = customerProfile.referralType
        val discount = 0

        when (adjustmentType) {
            ReferralType.BALANCE_TRANSFER -> {
                val khataBalance =
                    getIntOrZero(prevDue) + getIntOrZero(deliverAmount) - getIntOrZero(paid) - discount
                val msg = "$adjustmentType: ${this.name} -> $adjustmentWith > ₹$khataBalance"
                LogMe.log("Balance Transfer from '${this.name}' to '${adjustmentWith}' of amount: $khataBalance")
                MoneyAdjustments.addTransaction(this.name, khataBalance, adjustmentWith, khataBalance, adjustmentType, msg, msg)
            }

            ReferralType.CREDIT_PER_KG_SALE -> {
                val deliveredKg = getDoubleOrZero(this.deliveredKg)
                val bonusPerUnit = getDoubleOrZero(customerProfile.referralInput)
                val creditAmount = (deliveredKg * bonusPerUnit).toInt()
                val msg = "$adjustmentType: ${this.name} (${this.deliveredKg}kg) --> ₹$khataBalance"

                MoneyAdjustments.addTransaction(this.name, 0, adjustmentWith, creditAmount, adjustmentType, msg, msg)
                LogMe.log("Adding amount: Name: ${this.name} - ${getIntOrZero(this.khataBalance)}")
            }

            ReferralType.NONE -> {
            }
        }
    }

    companion object {
        private fun getDeliverObjectFromOrder(
            name: String,
            orderedPc: String,
            orderedKg: String
        ): DeliverToCustomerDataModel {
            return DeliverToCustomerDataModel(
                id = "${System.currentTimeMillis()}",
                timestamp = DateUtils.getCurrentTimestamp(),
                name = name,
                orderedPc = orderedPc,
                orderedKg = orderedKg,
                rate = "${CustomerDataUtils.getDeliveryRate(name)}",
                customerAccount = name,
                deliveryStatus = "DELIVERING"
            )
        }

        fun getDeliverObjectFromOrder(order: SMSOrderModel): DeliverToCustomerDataModel {
            return getDeliverObjectFromOrder(
                order.name,
                order.orderedPc.toString(),
                order.orderedKg.toString()
            )
        }

        fun getDeliverObjectFromOrder(name: String): DeliverToCustomerDataModel {
            return getDeliverObjectFromOrder(name, "0", "0")
        }
    }
}


class MoneyAdjustments {

    var from: MoneyAdjustmentEntry = MoneyAdjustmentEntry()
    var to: MoneyAdjustmentEntry = MoneyAdjustmentEntry()
    lateinit var referralType: ReferralType

    class MoneyAdjustmentEntry {
        lateinit var name: String
        var debit: Int = 0
        var credit: Int = 0
        lateinit var msg: String

        fun addDebit(name: String, amount: Int, msg: String) {
            LogMe.log("Amount Deduction Registered for '$name': Rs $amount")
            this.name = name
            this.debit = amount
            this.credit = 0
            this.msg = msg
        }

        fun addCredit(name: String, amount: Int, msg: String) {
            LogMe.log("Amount Credit Registered for '$name': Rs $amount")
            this.name = name
            this.debit = 0
            this.credit = amount
            this.msg = msg
        }
    }


    companion object {

        fun addTransaction(
            from: String,
            debitAmount: Int,
            to: String,
            creditAmount: Int,
            referralType: ReferralType,
            debitMsg: String,
            creditMsg: String
        ) {
            if (from == to) {
                throw Exception("Trying to add adjustment from $from to $to. Same person adjustments are not allowed.")
            }

            var dataObj = getDataIfAvailable(from, to, referralType)
            if (dataObj == null) {
                dataObj = MoneyAdjustments()
                DeliveryCalculations.adjustments.add(dataObj)
            }

            dataObj.referralType = referralType
            dataObj.from.addDebit(from, debitAmount, debitMsg)
            dataObj.to.addCredit(to, creditAmount, creditMsg)
        }

        private fun getDataIfAvailable(
            fromName: String,
            toName: String,
            referralType: ReferralType
        ): MoneyAdjustments? {
            DeliveryCalculations.adjustments.forEach {
                if (it.from.name == fromName && it.to.name == toName && referralType == it.referralType) {
                    return it
                }
            }
            return null
        }

        fun getAdjustmentAmount(name: String): Int {
            var sumAdjustment = 0
            DeliveryCalculations.adjustments.forEach {
                if (it.to.name == name) {
                    sumAdjustment += it.to.credit
                    sumAdjustment -= it.to.debit
                }
                if (it.from.name == name) {
                    sumAdjustment += it.from.credit
                    sumAdjustment -= it.from.debit
                }
            }
            return sumAdjustment
        }

        fun getAdjustmentMessages(name: String): String {
            var msg = ""
            DeliveryCalculations.adjustments.forEach {
                if (it.to.name == name) {
                    msg += it.to.msg
                }
                if (it.from.name == name) {
                    msg += it.from.msg
                }
            }
            return msg
        }
    }
}