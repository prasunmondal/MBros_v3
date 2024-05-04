package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Payments.Staged.StagedPaymentsModel
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class SMSParser {

    companion object {

        fun parseWithMetadata(smsDetail: OSMSModel): List<SMS>? {
            val metadata = SingleAttributedDataUtils.getRecords()
            if (smsDetail.inputData.equals(metadata.load_account, true)) {
                val templateToSendInfo = smsDetail.dataTemplate

                val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
                val text = templateToSendInfo
                    .replace("<date>", formattedDate)
                    .replace("<loadPc>", metadata.actualLoadPc)
                    .replace("<loadKg>", metadata.actualLoadKg)
                    .replace("<loadCompanyName>", metadata.load_companyName)
                    .replace("<shortage>", DeliveryCalculations.getShortage(metadata.actualLoadKg, DeliveryCalculations.getTotalDeliveredKg().toString()).toString())
                    .replace("<km>", DeliveryCalculations.getKmDiff(metadata.vehicle_finalKm).toString())
                    .replace("<policeBreakdown>", metadata.police_breakdown)
                    .replace("<labourExpense>", SingleAttributedDataUtils.getExtraExpenseExcludingPolice(metadata).toString())

                return listOf(SMS(smsDetail.platform, smsDetail.sendTo, text))
            } else {
                return null
            }
        }

        fun parseWithDeliveryData(template: String, deliveryData: DeliverToCustomerDataModel): String {
            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            return template.replace("<date>", formattedDate)
                .replace("<name>", deliveryData.name)
                .replace("<prevDue>", deliveryData.prevDue)
                .replace("<pc>", deliveryData.deliveredPc)
                .replace("<kg>", deliveryData.deliveredKg)
                .replace("<todaysAmount>", deliveryData.deliverAmount)
                .replace("<paidAmount>", deliveryData.paid)
                .replace("<rate>", deliveryData.rate)
                .replace("<otherBalances>", deliveryData.otherBalances)
                .replace("<balanceIncludingOtherBalances>", deliveryData.totalBalance)
                .replace("<balanceExcludingOtherBalances>", deliveryData.khataBalance)
        }

        fun parseWithStagedPaymentDetails(template: String, stagedPayments: StagedPaymentsModel): String {
            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            return template.replace("<date>", formattedDate)
                .replace("<name>", stagedPayments.name)
                .replace("<prevDue>", stagedPayments.balanceBeforePayment)
                .replace("<paidAmount>", stagedPayments.paidAmount)
                .replace("<balanceIncludingOtherBalances>", stagedPayments.newBalance)
                .replace("<balanceExcludingOtherBalances>", "stagedPayments.khataBalance")
        }

        fun sendDeliverySMS(smsDetail: OSMSModel): List<SMS>? {
            LogMe.log(smsDetail.toString())

            // if delivery data is not available for the customer, send null - no communication required
            val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(smsDetail.inputData) ?: return null
            val isEnabled = deliveryData.totalBalance.isNotEmpty()
            if (!isEnabled)
                return null

            val text = parseWithDeliveryData(smsDetail.inputData, deliveryData)
            LogMe.log("$smsDetail: $text")
            return listOf(SMS(smsDetail.platform, smsDetail.sendTo, text))
        }

        fun sendViaDesiredMedium(medium: String, number: String, text: String) {
            when (medium) {
                "SMS" -> SMSUtils.sendSMS(AppContexts.get(), text, number)
                "WHATSAPP" -> Whatsapp.sendMessage(AppContexts.get(), number, text)
            }
        }

        fun deliverySMSToNonZeroKgCustomers(smsDetail: OSMSModel): List<SMS>? {
            LogMe.log(smsDetail.toString())

            val entries = smsDetail.sendTo.split(",")
            val list: MutableList<SMS> = mutableListOf()

            entries.forEach {entry ->
                val customerName =  entry.trim().split(":")[0].trim()
                val customerNumber =  entry.trim().split(":")[1].trim()
                // if delivery data is not available for the customer, send null - no communication required
                val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(customerName)

                if(deliveryData!=null && NumberUtils.getDoubleOrZero(deliveryData.deliveredKg)>0.0) {
                    val text = parseWithDeliveryData(smsDetail.inputData, deliveryData)
                    LogMe.log("$smsDetail: $text")
                    list.add(SMS(smsDetail.platform, customerNumber, text))
                }
            }

            if(list.isEmpty())
                return null
            return list
        }

        fun deliverySMSToOnlyPaymentCustomers(smsDetail: OSMSModel): List<SMS>? {
            LogMe.log(smsDetail.toString())

            val customerNumberNameMap = getCustomerNumberNameMap(smsDetail)
            val list: MutableList<SMS> = mutableListOf()

            customerNumberNameMap.forEach { (number, name) ->
                // if delivery data is not available for the customer, send null - no communication required
                val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(name)

                if(deliveryData!=null
                    && NumberUtils.getDoubleOrZero(deliveryData.deliveredKg)==0.0
                    && NumberUtils.getDoubleOrZero(deliveryData.paid) > 0) {

                    val text = parseWithDeliveryData(smsDetail.inputData, deliveryData)
                    LogMe.log("$smsDetail: $text")
                    list.add(SMS(smsDetail.platform, number, text))
                }
            }

            if(list.isEmpty())
                return null
            return list
        }
        
        fun getCustomerNumberNameMap(smsDetail: OSMSModel): Map<String, String> {
            val entries = smsDetail.sendTo.split(",")
            val map: MutableMap<String, String> = mutableMapOf()

            entries.forEach { entry ->
                val customerName = entry.trim().split(":")[0].trim()
                val customerNumber = entry.trim().split(":")[1].trim()
                map[customerNumber] = customerName
            }
            return map
        }

        fun paymentCommunication(smsDetail: OSMSModel, inputObj: Any?): List<SMS> {
            val stagedPayment = inputObj as StagedPaymentsModel
            val list: MutableList<SMS> = mutableListOf()

            val customerNumberNameMap = getCustomerNumberNameMap(smsDetail)
            customerNumberNameMap.forEach { (number, name) ->
                    if (name == stagedPayment.name) {
                        val text = parseWithStagedPaymentDetails(smsDetail.dataTemplate, inputObj)
                        list.add(SMS(smsDetail.platform, number, text))
                    }
                }

            list.forEach {
                LogMe.log(it.toString())
            }
              return list
        }
    }
}