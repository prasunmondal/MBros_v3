package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.OneShot.Delivery.ReferralType
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getIntOrZero


object DeliveringUtils : GSheetSerialized<DeliverToCustomerDataModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURLNew,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "deliverOrders",
    query = null,
    modelClass = DeliverToCustomerDataModel::class.java
) {

    fun get(name: String, useCache: Boolean = true): DeliverToCustomerDataModel? {
        val list = DeliveringUtils.fetchAll(useCache).execute()
        val filteredObj = list.filter { it.name == name }
        if(filteredObj.isEmpty())
            return null
        return filteredObj[0]
    }

    fun payOnline(name: String, amount: Int) {
        var obj = get(name)
        if(obj == null)
            obj = DeliverToCustomerDataModel()
        obj.paidOnline = amount.toString()
        calculateAll(obj)
    }

    fun payCash(name: String, amount: Int) {
        var obj = get(name)
        if(obj == null)
            obj = DeliverToCustomerDataModel()
        obj.paidCash = amount.toString()
        calculateAll(obj)
    }

    fun calculateAll(obj: DeliverToCustomerDataModel): DeliverToCustomerDataModel {
        val kg = NumberUtils.getDoubleOrZero(obj.deliveredKg)
        val rate = getIntOrZero(obj.rate)
        val deliveredAmount = calculateDeliverAmount(kg, rate)
        val prevAmount = getIntOrZero(obj.prevDue)
        val paidCash = getIntOrZero(obj.paidCash)
        val paidOnline = getIntOrZero(obj.paidOnline)
        val adjustments = referAdjustBalanceTransfer(obj)
        val discounts = 0

        obj.deliverAmount = deliveredAmount.toString()
        obj.paid = (getIntOrZero(obj.paidCash) + getIntOrZero(obj.paidOnline)).toString()
        obj.khataBalance = (prevAmount + deliveredAmount + adjustments - discounts - paidCash - paidOnline).toString()
        obj.otherBalances = CustomerKYC.getByName(obj.name)!!.otherBalances
        obj.totalBalance = (getIntOrZero(obj.khataBalance) + getIntOrZero(obj.otherBalances)).toString()
        return obj
    }

    private fun referAdjustBalanceTransfer(deliveryObj: DeliverToCustomerDataModel): Int {
        val preprocess: MutableList<String> = mutableListOf()
        val postprocess: MutableList<String> = mutableListOf()

        // add all the names which should be processed before this gets processed
        CustomerKYC.fetchAll().execute().filter {
            it.referredBy == deliveryObj.name
        }.forEach {
            if(it.referredBy.isNotEmpty())
                preprocess.add(it.nameEng)
        }

        // add all the names which should be processed after this gets processed
        CustomerKYC.fetchAll().execute().filter {
            it.nameEng == deliveryObj.name
        }.forEach {
            if(it.referredBy.isNotEmpty())
                postprocess.add(it.referredBy)
        }

        var adjustment = 0
        preprocess.forEach { name ->
            val adjustmentType = CustomerKYC.getCustomerByEngName(name)!!.referralType
            when(adjustmentType) {
                ReferralType.BALANCE_TRANSFER -> {
                    val obj = get(name)
                    if(obj != null)
                        adjustment -= getIntOrZero(calculateAll(obj).khataBalance)
                    LogMe.log("Transferring amount: Name: ${deliveryObj.name} - $adjustment")
                }
                ReferralType.CREDIT_PER_KG_SALE -> {
                    LogMe.log("Deducting amount: Name: ${deliveryObj.name} - $adjustment")
                    adjustment = getIntOrZero(deliveryObj.deliveredKg)
                }
                ReferralType.NONE -> {
                }
            }
        }

        postprocess.forEach { name ->
            val adjustmentType = CustomerKYC.getCustomerByEngName(name)!!.referralType
            when(adjustmentType) {
                ReferralType.BALANCE_TRANSFER -> {
                    val obj = get(name)
                    if(obj != null)
                        adjustment -= getIntOrZero(calculateAll(obj).khataBalance)
                    LogMe.log("Transferring amount: Name: ${deliveryObj.name} - $adjustment")
                }
                ReferralType.CREDIT_PER_KG_SALE -> {
                    LogMe.log("Deducting amount: Name: ${deliveryObj.name} - $adjustment")
                    adjustment = getIntOrZero(deliveryObj.deliveredKg)
                }
                ReferralType.NONE -> {
                }
            }
        }

//            ReferralType.BALANCE_TRANSFER -> {
//                preprocess.forEach {
//                    adjustment += getIntOrZero(calculateAll(get(it)!!).khataBalance)
//                }
////                postprocess.forEach {
////                    adjustment -= getIntOrZero(calculateAll(get(it)!!).khataBalance)
////                }
//                LogMe.log("Transferring amount: Name: ${deliveryObj.name} - $adjustment")
//                return adjustment
//            }
//            ReferralType.CREDIT_PER_KG_SALE -> {
//                LogMe.log("Deducting amount: Name: ${deliveryObj.name} - $adjustment")
//                return getIntOrZero(deliveryObj.deliveredKg)
//            }
//            ReferralType.NONE -> {
//                return 0
//            }
        return adjustment
    }

    private fun calculateDeliverAmount(kg: Double, rate: Int): Int {
        val roundUpOffset = 0.000001
        return (kg * rate + roundUpOffset).toInt()
    }

    fun calculateDeliverAmount(kg: String, rate: String): Int {
        return calculateDeliverAmount(NumberUtils.getDoubleOrZero(kg), getIntOrZero(rate))
    }
}