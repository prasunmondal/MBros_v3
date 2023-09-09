package com.tech4bytes.mbrosv3.AppData.RemoteAppConstants

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

enum class AppConstants {
    SMS_ORDER_SEQUENCE,
    SMS_ORDER_GET_ORDER_PH_NUMBER,
    CAR_RATE_PER_KM,
    DRIVER_SALARY,
    SEND_DELIVERY_DETAILS_BY_SMS,
    DELIVERY_BASE_RATE_DIFF,
    WHATSAPP_NUMBER_MONTU,
    SEND_LOAD_INFO_TEMPLATE_MONTU;

    companion object {
        fun fetchAll(useCache: Boolean = true) {
            AppConstantsUtil.getAll(useCache)
        }

        fun get(constant: AppConstants, useCache: Boolean = true): String {
            LogMe.log("Searching in AppConstants: $constant")
            val constantList = AppConstantsUtil.getAll(useCache)
            constantList.forEach {
                if (it.constantName == constant) {
                    LogMe.log("Searching in AppConstants: $constant: Successful. Value: ${it.constantValue}")
                    return it.constantValue
                }
            }
            LogMe.log("Searching in AppConstants: $constant: Unsuccessful.")
            return ""
        }

        fun get(constant: String, useCache: Boolean = true): String {
            LogMe.log("Searching in AppConstants: $constant")
            val constantList = AppConstantsUtil.getAll(useCache)
            constantList.forEach {
                if (it.constantName != null && it.constantName.toString() == constant) {
                    LogMe.log("Searching in AppConstants: $constant: Successful. Value: ${it.constantValue}")
                    return it.constantValue
                }
            }
            LogMe.log("Searching in AppConstants: $constant: Unsuccessful.")
            return ""
        }
    }
}