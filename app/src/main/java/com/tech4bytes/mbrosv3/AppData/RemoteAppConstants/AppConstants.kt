package com.tech4bytes.mbrosv3.AppData.RemoteAppConstants

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.util.Locale

enum class AppConstants {
    SMS_ORDER_SEQUENCE,
    SMS_ORDER_GET_ORDER_PH_NUMBER,
    CAR_RATE_PER_KM,
    DRIVER_SALARY,
    SEND_DELIVERY_DETAILS_BY_SMS,
    DELIVERY_BASE_RATE_DIFF,
    ADD_TO_FUELING_KMS_TO_GET_FINAL_KM,
    DUE_SHOW_BALANCE_AVG_DAYS,
    SMS_ORDERING_SHOW_SMS_FOR_N_DAYS,
    SMS_NUMBER_ON_LOAD_INFO_UPDATE,
    SMS_NUMBER_ON_DEVICE_REG_REQUEST,
    FUEL_OIL_RATE_UPPER_LIMIT,
    FUEL_OIL_RATE_LOWER_LIMIT,
    WEB_PORTAL_URL,
    EVENTS_SHOW_N_DAYS;

    companion object {

        fun get(constant: AppConstants, useCache: Boolean = true): String {
            LogMe.log("Searching in AppConstants: $constant")
            val constantList = AppConstantsUtil.fetchAll(useCache).execute()
            constantList.forEach {
                if (it.constantName == constant.name) {
                    LogMe.log("Searching in AppConstants: $constant: Successful. Value: ${it.constantValue}")
                    return it.constantValue
                }
            }
            LogMe.log("Searching in AppConstants: $constant: Unsuccessful.")
            return ""
        }

        fun get(constant: String, useCache: Boolean = true): String {
            LogMe.log("Searching in AppConstants: $constant")
            val constantList = AppConstantsUtil.fetchAll(useCache).execute()
            constantList.forEach {
                if (it.constantName == constant) {
                    LogMe.log("Searching in AppConstants: $constant: Successful. Value: ${it.constantValue}")
                    return it.constantValue
                }
            }
            LogMe.log("Searching in AppConstants: $constant: Unsuccessful.")
            return ""
        }
    }

    class GeneratedKeys {
        companion object {
            fun getWhatsappNumber(accountName: String): String {
                return get(("WHATSAPP_NUMBERS_$accountName").uppercase(Locale.ROOT))
            }

            fun getTemplateToSendInfo(accountName: String): String {
                return get(("SEND_LOAD_INFO_TEMPLATE_$accountName").uppercase(Locale.ROOT))
            }
        }
    }
}