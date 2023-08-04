package com.tech4bytes.mbrosv3.AppData.RemoteAppConstants

enum class AppConstants {
    SMS_ORDER_SEQUENCE;

    companion object {
        fun fetchAll(useCache: Boolean = true) {
            AppConstantsUtil.getAll(useCache)
        }

        fun get(constant: AppConstants, useCache: Boolean = true): String? {
            val constantList = AppConstantsUtil.getAll(useCache)
            constantList.forEach {
                if(it.constantName == constant) {
                    return it.constantValue
                }
            }
            return null
        }
    }
}