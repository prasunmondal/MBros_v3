package com.tech4bytes.mbrosv3.Sms.SMSProcessors.SMSProcessor

import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OSMS
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OSMSModel
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.SMS
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.SMSParser
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class SMSProcessor {
    companion object {
        fun getSMSList(selectedCommunications: String, inputObj: Any? = null): MutableList<SMS> {
            LogMe.log("Getting messages for type: $selectedCommunications")
            val smsList: MutableList<SMS> = mutableListOf()
            OSMS.fetchAll().execute().forEach {
                LogMe.log("Looking for: $selectedCommunications");
                getIndividualCommType(it).forEach {r ->
                    LogMe.log("..$r..")
                }

                getIndividualCommType(it).forEach {
                    LogMe.log(it)
                }
                if (it.isAuthorized() && it.isMsgEnabled(it) && getIndividualCommType(it).contains(selectedCommunications)) {
                    LogMe.log("Processing: $selectedCommunications")
                    val smsResult: List<SMS>? = when (it.communicationType) {
                        "DELIVERY_SMS" -> SMSParser.sendDeliverySMS(it)
                        "DAY_SUMMARY", "LOAD_DETAILS" -> SMSParser.parseWithMetadata(it)
                        "DELIVERY_SMS_ONLY_NON_ZERO_KG" -> SMSParser.deliverySMSToNonZeroKgCustomers(it)
                        "DELIVERY_SMS_ONLY_PAYMENT" -> SMSParser.deliverySMSToOnlyPaymentCustomers(it)
                        "PAYMENT_INTIMATION" -> SMSParser.paymentCommunication(it, inputObj)
                        else -> null
                    }
                    if (smsResult != null)
                        smsList.addAll(smsResult)
                }
            }
            smsList.forEach { sms ->
                LogMe.log(sms.toString())
            }
            return smsList
        }

        private fun getIndividualCommType(oSmsModel: OSMSModel): List<String> {
            return oSmsModel.commReceiverCategory.split(",")
        }
    }
}