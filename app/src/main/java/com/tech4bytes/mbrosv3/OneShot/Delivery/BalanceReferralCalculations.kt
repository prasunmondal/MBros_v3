package com.tech4bytes.mbrosv3.OneShot.Delivery

import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class BalanceCalculatingObj {
    var from: String
    var to: String
    var referral_type: ReferralType
    var transferAmount: Int = 0
    var balanceOfReferered = 0
    var message: String = ""

    constructor(from: String, to: String, referral_type: ReferralType, transferAmount: Int) {
        this.from = from
        this.to = to
        this.referral_type = referral_type
        this.transferAmount = transferAmount
    }

    override fun toString(): String {
        return "BalanceCalculatingObj(from='$from', to='$to', referral_type=$referral_type, transferAmount=$transferAmount, balanceOfReferered='$balanceOfReferered', message='$message')"
    }


}

enum class ReferralType {
    BALANCE_TRANSFER,
    CREDIT_PER_KG_SALE,
    NONE
}

object BalanceReferralCalculations {

    val referralBalanceMap: MutableMap<String, BalanceCalculatingObj> = mutableMapOf()

    fun calculate(deliverObjOfCustomer: DeliverToCustomerDataModel) {
        val customerDetails = CustomerKYC.getByName(deliverObjOfCustomer.name)!!
        val referralType = customerDetails.referralType
        if (referralType == ReferralType.NONE)
            return
        var transferAmount = 0
        val result = BalanceCalculatingObj(deliverObjOfCustomer.name, deliverObjOfCustomer.customerAccount, referralType, transferAmount)
        result.to = customerDetails.customerAccount

        when (referralType) {
            ReferralType.NONE -> {
                transferAmount = 0
                result.balanceOfReferered = NumberUtils.getIntOrZero(deliverObjOfCustomer.balanceDue)
            }
            ReferralType.BALANCE_TRANSFER -> {
                transferAmount = (NumberUtils.getIntOrZero(deliverObjOfCustomer.prevDue)
                        + NumberUtils.getIntOrZero(deliverObjOfCustomer.todaysAmount)
                        - NumberUtils.getIntOrZero(deliverObjOfCustomer.paid))
                result.message = "Transferred Rs $transferAmount from ${result.from} to ${result.to} for rule ${result.referral_type}."
                result.balanceOfReferered = 0
            }
            ReferralType.CREDIT_PER_KG_SALE -> {
                LogMe.log("deliverObjOfCustomer.deliveredKg: " + deliverObjOfCustomer.deliveredKg)
                LogMe.log("deliverObjOfCustomer.deliveredKg: " + NumberUtils.getDoubleOrZero(deliverObjOfCustomer.deliveredKg))
                LogMe.log("customerDetails.referralInput: " + customerDetails.referralInput)
                LogMe.log("customerDetails.referralInput: " + NumberUtils.getIntOrZero(customerDetails.referralInput))
                LogMe.log("Total: " + (NumberUtils.getDoubleOrZero(deliverObjOfCustomer.deliveredKg)
                        * NumberUtils.getIntOrZero(customerDetails.referralInput)).toString())
                val s1= NumberUtils.getDoubleOrZero(deliverObjOfCustomer.deliveredKg)
                val s2 = NumberUtils.getIntOrZero(customerDetails.referralInput)
                val s4 = NumberUtils.getDoubleOrZero((s1 * s2).toString()).toInt()
                transferAmount = -s4
                LogMe.log("transferAmount: $transferAmount")
                result.balanceOfReferered = NumberUtils.getIntOrZero(deliverObjOfCustomer.balanceDue)
                result.message = "Transferred Rs $transferAmount from ${result.from} to ${result.to} for rule ${result.referral_type}."
            }
        }
        result.transferAmount = transferAmount
        referralBalanceMap.put("${deliverObjOfCustomer.customerAccount} - ${deliverObjOfCustomer.name}", result)
//        return result
    }

    fun getTotalDiscountFor(name: String): BalanceCalculatingObj {
        LogMe.log("Totalling discounts for: $name")
        val result = BalanceCalculatingObj("TOTAL", name, ReferralType.NONE, 0)
        result.message = ""
        referralBalanceMap.forEach { (key, balanceCalculatingObj) ->
            if(balanceCalculatingObj.to == name) {
                LogMe.log("--- Selected: $balanceCalculatingObj")
                result.transferAmount += balanceCalculatingObj.transferAmount
                result.message += balanceCalculatingObj.message + "\n"
            } else {
                LogMe.log("Not Selected: $balanceCalculatingObj")
            }
        }
        return result
    }
}