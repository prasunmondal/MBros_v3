package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

class OSMSProcessor {

    companion object {

        fun sendLoadDetails(smsDetail: OSMSModel): SMS? {
            val metadata = SingleAttributedData.getRecords()
            if (smsDetail.inputData.equals(metadata.load_account, true)) {
                val templateToSendInfo = smsDetail.dataTemplate

                val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
                val text = templateToSendInfo
                    .replace("<date>", formattedDate)
                    .replace("<loadPc>", SingleAttributedData.getRecords().actualLoadPc)
                    .replace("<loadKg>", SingleAttributedData.getRecords().actualLoadKg)
                    .replace("<loadCompanyName>", metadata.load_companyName)

                return SMS(smsDetail.platform, smsDetail.sendTo, text)
            } else {
                return null
            }
        }

        fun sendDaySummary(smsDetail: OSMSModel): SMS {
            val metadata = SingleAttributedData.getRecords()
            val templateToSendInfo = smsDetail.dataTemplate

            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            val text = templateToSendInfo
                .replace("<date>", formattedDate)
                .replace("<loadPc>", metadata.actualLoadPc)
                .replace("<loadKg>", metadata.actualLoadKg)
                .replace("<shortage>", DeliveryCalculations.getShortage(metadata.actualLoadKg, DeliveryCalculations.getTotalDeliveredKg().toString()).toString())
                .replace("<km>", DeliveryCalculations.getKmDiff(metadata.vehicle_finalKm).toString())

            return SMS(smsDetail.platform, smsDetail.sendTo, text)
        }

        fun sendDeliverySMS(smsDetail: OSMSModel): SMS {
            val deliveryData = DeliverToCustomerActivity.getDeliveryRecord(smsDetail.inputData)!!
            val templateToSendInfo = smsDetail.dataTemplate
            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")

            val text = templateToSendInfo
                .replace("<date>", formattedDate)
                .replace("<pc>", deliveryData.deliveredPc)
                .replace("<kg>", deliveryData.deliveredKg)
                .replace("<paidAmount>", deliveryData.paid)
                .replace("<rate>", deliveryData.rate)
                .replace("<balanceAmount>", deliveryData.balanceDue)

            return SMS(smsDetail.platform, smsDetail.sendTo, text)
        }

        fun sendViaDesiredMedium(medium: String, number: String, text: String) {
            when (medium) {
                "SMS" -> SMSUtils.sendSMS(AppContexts.get(), text, number)
                "WHATSAPP" -> Whatsapp.sendMessage(AppContexts.get(), number, text)
            }
        }
    }
}