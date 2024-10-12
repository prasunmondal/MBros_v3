package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.BusinessData.DayMetadata
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Payments.Staged.StagedPaymentsModel
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.regex.Pattern


class SMSParser {

    companion object {

        fun parseWithMetadata(smsDetail: OSMSModel): List<SMS>? {
            val metadata = DayMetadata.getRecords()
            if (smsDetail.inputData.equals(metadata.load_account, true) || smsDetail.inputData.isEmpty()) {
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
                    .replace("<labourExpense>", DayMetadata.getExtraExpenseExcludingPolice(metadata).toString())

                return listOf(SMS(smsDetail.platform, smsDetail.sendTo, text))
            } else {
                return null
            }
        }

        fun parseWithDeliveryData(template: String, deliveryData: DeliverToCustomerDataModel): String {
           val splitedLines= template.split("\n")
            var data = ""
            splitedLines.forEach{line ->
                if (line.isEmpty() || formatString(line,deliveryData) != "") {
                    data +=  formatString(line,deliveryData)+"\n"
                }
            }
            return data.trim()
        }

        private fun formatString(inputLine: String, deliveryData: DeliverToCustomerDataModel):String{
            var stringLine = inputLine

            var conditionalParameterMap = getConditionalParameters(stringLine)
            if(conditionalParameterMap != null) {
                while (conditionalParameterMap != null) {
                    if (conditionalParameterMap["condition"]!!.isNotEmpty()) {
                        return if (isConditionTrue(conditionalParameterMap, deliveryData)) {
                            formatString(conditionalParameterMap["trueStatement"]!!, deliveryData)
                        } else {
                            formatString(conditionalParameterMap["falseStatement"]!!, deliveryData)
                        }
                    }
                    conditionalParameterMap = getConditionalParameters(stringLine)
                }
            }
            else {
                while (getNextVariable(stringLine) != null) {
                    val parameter = getNextVariable(stringLine)!!
                    val parameterValue = getValue(parameter, deliveryData)
                    stringLine = stringLine.replace("<$parameter>", parameterValue)
                }
            }
            return stringLine
        }

        private fun isConditionTrue(conditionalParameterMap: MutableMap<String, String>,
                                    deliveryData: DeliverToCustomerDataModel): Boolean {
            val conditionString = conditionalParameterMap["condition"]
            return (!conditionString.isNullOrEmpty()
                && NumberUtils.getDoubleOrZero(getValue(conditionString, deliveryData)) > 0.0)
        }

        fun getConditionalParameters(inputLine: String): MutableMap<String, String>? {
            val regex = "\\$(.*?)##(.*?)##(.*?)\\$"

            // Compile the pattern
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(inputLine)

            val map = mutableMapOf<String, String>()
            if (matcher.find()) {
                map.put("condition", matcher.group(1))
                map.put("trueStatement", matcher.group(2))
                map.put("falseStatement", matcher.group(3))
            } else {
                println("No match found.")
                return null
            }
            return map
        }

        fun getNextVariable(inputLine: String): String? {
            val regex = "<(.*?)>"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(inputLine)

            if (matcher.find()) {
                return matcher.group(1)
            }
            return null
        }

        fun getValue(varNAme: String, deliveryData: DeliverToCustomerDataModel): String{
            return when(varNAme){
                "date"-> DateUtils.getDateInFormat("dd/MM/yyyy")
                "name"-> deliveryData.name
                "rate"-> deliveryData.rate
                "prevDue"-> deliveryData.prevDue
                "pc"-> deliveryData.deliveredPc
                "kg"-> deliveryData.deliveredKg
                "todaysAmount"-> deliveryData.deliverAmount
                "paidCash"-> deliveryData.paidCash
                "paidOnline"-> deliveryData.paidOnline
                "paidAmount" -> deliveryData.paid
                "otherBalances"-> deliveryData.otherBalances
                "balanceIncludingOtherBalances"-> deliveryData.totalBalance
                "balanceExcludingOtherBalances"-> deliveryData.khataBalance
                else -> ""
            }
        }

        fun parseWithStagedPaymentDetails(template: String, stagedPayments: StagedPaymentsModel): String {
            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            return template.replace("<date>", formattedDate)
                .replace("<name>", stagedPayments.name)
                .replace("<prevDue>", stagedPayments.balanceBeforePayment)
                .replace("<paidAmount>", stagedPayments.paidAmount)
                .replace("<paymentMode>", stagedPayments.paymentMode)
                .replace("<balanceIncludingOtherBalances>", stagedPayments.newBalance)
                .replace("<balanceExcludingOtherBalances>", "stagedPayments.khataBalance")
        }

        fun sendViaDesiredMedium(medium: String, number: String, text: String) {
            when (medium) {
                "SMS" -> SMSUtils.sendSMS(AppContexts.get(), text, number)
                "WHATSAPP" -> Whatsapp.sendMessage(AppContexts.get(), number, text)
            }
        }

        fun deliverySMSToTransactingCustomers(smsDetail: OSMSModel): List<SMS>? {
            LogMe.log(smsDetail.toString())

            val entries = smsDetail.sendTo.split(",")
            val list: MutableList<SMS> = mutableListOf()

            entries.forEach {entry ->
                val customerName =  entry.trim().split(":")[0].trim()
                val customerNumber =  entry.trim().split(":")[1].trim()
                // if delivery data is not available for the customer, send null - no communication required
                val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(customerName)

                if(deliveryData!=null
                    && (NumberUtils.getDoubleOrZero(deliveryData.deliveredKg) > 0.0
                    || NumberUtils.getDoubleOrZero(deliveryData.paid) > 0)) {
                    val text = parseWithDeliveryData(smsDetail.dataTemplate, deliveryData)
                    LogMe.log("$smsDetail: $text")
                    list.add(SMS(smsDetail.platform, customerNumber, text))
                }
            }

            if(list.isEmpty())
                return null
            return list
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
                    val text = parseWithDeliveryData(smsDetail.dataTemplate, deliveryData)
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

                    val text = parseWithDeliveryData(smsDetail.dataTemplate, deliveryData)
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