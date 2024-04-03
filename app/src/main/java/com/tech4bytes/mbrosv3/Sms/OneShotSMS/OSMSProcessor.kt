package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class OSMSProcessor {

    companion object {

        fun sendLoadDetails(smsDetail: OSMSModel): List<SMS>? {
            val metadata = SingleAttributedDataUtils.getRecords()
            if (smsDetail.inputData.equals(metadata.load_account, true)) {
                val templateToSendInfo = smsDetail.dataTemplate

                val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
                val text = templateToSendInfo
                    .replace("<date>", formattedDate)
                    .replace("<loadPc>", SingleAttributedDataUtils.getRecords().actualLoadPc)
                    .replace("<loadKg>", SingleAttributedDataUtils.getRecords().actualLoadKg)
                    .replace("<loadCompanyName>", metadata.load_companyName)

                return listOf(SMS(smsDetail.platform, smsDetail.sendTo, text))
            } else {
                return null
            }
        }

        fun sendDaySummary(smsDetail: OSMSModel): List<SMS>? {
            val metadata = SingleAttributedDataUtils.getRecords()
            val templateToSendInfo = smsDetail.dataTemplate

            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            val text = templateToSendInfo
                .replace("<date>", formattedDate)
                .replace("<loadPc>", metadata.actualLoadPc)
                .replace("<loadKg>", metadata.actualLoadKg)
                .replace("<shortage>", DeliveryCalculations.getShortage(metadata.actualLoadKg, DeliveryCalculations.getTotalDeliveredKg().toString()).toString())
                .replace("<km>", DeliveryCalculations.getKmDiff(metadata.vehicle_finalKm).toString())

            return listOf(SMS(smsDetail.platform, smsDetail.sendTo, text))
        }

        fun sendDeliverySMS(smsDetail: OSMSModel): List<SMS>? {
            LogMe.log(smsDetail.toString())

            // if delivery data is not available for the customer, send null - no communication required
            val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(smsDetail.inputData) ?: return null

            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            val replaceMethod = { template: String ->
                template.replace("<date>", formattedDate)
                    .replace("<name>", deliveryData.name)
                    .replace("<prevDue>", deliveryData.prevDue)
                    .replace("<pc>", deliveryData.deliveredPc)
                    .replace("<kg>", deliveryData.deliveredKg)
                    .replace("<todaysAmount>", deliveryData.todaysAmount)
                    .replace("<paidAmount>", deliveryData.paid)
                    .replace("<rate>", deliveryData.rate)
                    .replace("<balanceAmountIncludingLH>", deliveryData.khataDue)
                    .replace("<balanceAmountExcludingLH>", deliveryData.totalBalance)
            }

            LogMe.log(smsDetail.toString() + ": " + replaceMethod(smsDetail.enablement_template))
            val isEnabled = deliveryData.totalBalance.isNotEmpty()
            if (!isEnabled)
                return null

            val text = replaceMethod(smsDetail.dataTemplate)
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
                    val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
                    val replaceMethod = { template: String ->
                        template.replace("<date>", formattedDate)
                            .replace("<name>", deliveryData.name)
                            .replace("<prevDue>", deliveryData.prevDue)
                            .replace("<pc>", deliveryData.deliveredPc)
                            .replace("<kg>", deliveryData.deliveredKg)
                            .replace("<todaysAmount>", deliveryData.todaysAmount)
                            .replace("<paidAmount>", deliveryData.paid)
                            .replace("<rate>", deliveryData.rate)
                            .replace("<balanceAmount>", deliveryData.totalBalance)
                    }

                    LogMe.log(smsDetail.toString() + ": " + replaceMethod(smsDetail.enablement_template))
                    val text = replaceMethod(smsDetail.dataTemplate)
                    list.add(SMS(smsDetail.platform, customerNumber, text))
                }
            }

            if(list.isEmpty())
                return null
            return list
        }

        fun deliverySMSToOnlyPaymentCustomers(smsDetail: OSMSModel): List<SMS>? {
            LogMe.log(smsDetail.toString())

            val entries = smsDetail.sendTo.split(",")
            val list: MutableList<SMS> = mutableListOf()

            entries.forEach {entry ->
                val customerName =  entry.trim().split(":")[0].trim()
                val customerNumber =  entry.trim().split(":")[1].trim()
                // if delivery data is not available for the customer, send null - no communication required
                val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(customerName)

                if(deliveryData!=null
                    && NumberUtils.getDoubleOrZero(deliveryData.deliveredKg)==0.0
                    && NumberUtils.getDoubleOrZero(deliveryData.paid) > 0) {
                    val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
                    val replaceMethod = { template: String ->
                        template.replace("<date>", formattedDate)
                            .replace("<name>", deliveryData.name)
                            .replace("<prevDue>", deliveryData.prevDue)
                            .replace("<pc>", deliveryData.deliveredPc)
                            .replace("<kg>", deliveryData.deliveredKg)
                            .replace("<todaysAmount>", deliveryData.todaysAmount)
                            .replace("<paidAmount>", deliveryData.paid)
                            .replace("<rate>", deliveryData.rate)
                            .replace("<balanceAmount>", deliveryData.totalBalance)
                    }

                    LogMe.log(smsDetail.toString() + ": " + replaceMethod(smsDetail.enablement_template))
                    val text = replaceMethod(smsDetail.dataTemplate)
                    list.add(SMS(smsDetail.platform, customerNumber, text))
                }
            }

            if(list.isEmpty())
                return null
            return list
        }
    }
}