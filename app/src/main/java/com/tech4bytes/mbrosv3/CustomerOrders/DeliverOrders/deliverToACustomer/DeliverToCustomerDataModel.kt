package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import android.view.View
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliveringUtils.calculateDeliverAmount
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.SMSOrderModel
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.OneShot.Delivery.OSDDeliveryEntryInfo
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
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
) : java.io.Serializable {

    override fun toString(): String {
        return "DeliverToCustomerDataModel(id='$id', date='$date', timestamp='$timestamp', name='$name', orderedPc='$orderedPc', orderedKg='$orderedKg', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', todaysAmount='$deliverAmount', customerAccount='$customerAccount', prevDue='$prevDue', totalDue='$khataBalance', paidCash='$paidCash', paidOnline='$paidOnline', paid='$paid', balanceDue='$totalBalance', deliveryStatus='$deliveryStatus', discount='$adjustments', notes='$notes')"
    }

    fun setDeliveredPc(deliveredPc: Int, view: View) {
        this.deliveredPc = deliveredPc.toString()
        calculate(view)
    }

    fun setDeliveredKg(deliveredKg: Double, view: View) {
        this.deliveredKg = deliveredKg.toString()
        calculate(view)
    }

    fun setPaidCash(paidCash: Int, view: View) {
        this.paidCash = paidCash.toString()
        calculate(view)
    }

    fun setPaidOnline(paidOnline: Int, view: View) {
        this.paidOnline = paidOnline.toString()
        calculate(view)
    }

    fun setRate(rate: Int, view: View) {
        this.rate = rate.toString()
        calculate(view)
    }

    fun calculate(view: View?) {
        val deliveredAmount = calculateDeliverAmount(deliveredKg, rate)
//        val prevAmount = getIntOrZero(prevDue)
//        val adjustments = referAdjustBalanceTransfer()
        val adjustments = 0
        val discounts = 0

        prevDue = CustomerDueData.getLastFinalizedDue(name)
        deliverAmount = deliveredAmount.toString()
        paid = (getIntOrZero(paidCash) + getIntOrZero(paidOnline)).toString()
        khataBalance =
            (getIntOrZero(prevDue) + deliveredAmount + adjustments - discounts - getIntOrZero(paidCash) - getIntOrZero(
                paidOnline
            )).toString()
        otherBalances = CustomerKYC.getByName(name)!!.otherBalances
        totalBalance = (getIntOrZero(khataBalance) + getIntOrZero(otherBalances)).toString()

        if (view != null)
            OSDDeliveryEntryInfo.updateDerivedAttributesInUi(view, this)
    }

        companion object {
            fun getDeliverObjectFromOrder(order: SMSOrderModel): DeliverToCustomerDataModel {
            return DeliverToCustomerDataModel(
            id = "${System.currentTimeMillis()}",
            timestamp = DateUtils.getCurrentTimestamp(),
            name = order.name,
            orderedPc = order.orderedPc.toString(),
            orderedKg = order.orderedKg.toString(),
            rate = "${CustomerDataUtils.getDeliveryRate(order.name)}",
//            prevDue = CustomerDueData.getLastFinalizedDue(order.name),
            customerAccount = order.name,
            deliveryStatus = "DELIVERING"
            )
        }
    }
}