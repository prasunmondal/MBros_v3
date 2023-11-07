package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.SMSDetails

import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCUtils

class SendSMSDetailsUtils {

    companion object {

        fun getSendSMSDetailsNumber(name: String): String? {
            if (AppConstants.get(AppConstants.SEND_DELIVERY_DETAILS_BY_SMS).split(",").contains(name)) {
                return CustomerKYCUtils.getCustomerByEngName(name)?.smsNumber
            }
            return null
        }
    }
}