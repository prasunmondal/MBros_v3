package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

class OneShotSMS : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_sms)
        AppContexts.set(this)
    }

    fun onClickSendSMS(view: View) {
        processQueue()
    }

    fun processQueue() {
        OSMS.get().forEach {
            if (it.isEnabled.toBoolean()) {
                when (it.communicationType) {
                    "DELIVERY_SMS" -> sendDeliverySMS(it)
                    "DAY_SUMMARY" -> sendDaySummary(it)
                    "LOAD_DETAILS" -> sendLoadDetails(it)
                }
            }
        }
    }

    private fun sendLoadDetails(smsDetail: OSMSModel) {
        val metadata = SingleAttributedData.getRecords()
        if (smsDetail.inputData.equals(metadata.load_account, true)) {
            val templateToSendInfo = smsDetail.dataTemplate

            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            val text = templateToSendInfo
                .replace("<date>", formattedDate)
                .replace("<loadPc>", SingleAttributedData.getRecords().actualLoadPc)
                .replace("<loadKg>", SingleAttributedData.getRecords().actualLoadKg)
                .replace("<loadCompanyName>", metadata.load_companyName)
            sendViaDesiredMedium(smsDetail, text)
        }
    }

    private fun sendViaDesiredMedium(smsDetail: OSMSModel, text: String) {
        when (smsDetail.platform) {
            "SMS" -> SMSUtils.sendSMS(AppContexts.get(), text, smsDetail.sendTo)
            "WHATSAPP" -> Whatsapp.sendMessage(AppContexts.get(), smsDetail.sendTo, text)
        }
    }

    private fun sendDaySummary(smsDetail: OSMSModel) {
        val metadata = SingleAttributedData.getRecords()
        val templateToSendInfo = smsDetail.dataTemplate

        val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
        val text = templateToSendInfo
            .replace("<date>", formattedDate)
            .replace("<loadPc>", metadata.actualLoadPc)
            .replace("<loadKg>", metadata.actualLoadKg)
            .replace("<shortage>", DeliveryCalculations.getShortage(metadata.actualLoadKg, DeliveryCalculations.getTotalDeliveredKg().toString()).toString())
            .replace("<km>", DeliveryCalculations.getKmDiff(metadata.vehicle_finalKm).toString())
        sendViaDesiredMedium(smsDetail, text)
    }

    private fun sendDeliverySMS(smsDetail: OSMSModel) {
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

        sendViaDesiredMedium(smsDetail, text)
    }
}