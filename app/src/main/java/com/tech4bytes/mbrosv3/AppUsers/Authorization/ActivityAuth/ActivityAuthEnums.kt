package com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth

enum class ActivityAuthEnums : java.io.Serializable {
    ADMIN,
    DELIVERY,
    COLLECTOR,
    UNIDENTIFIED,
    ORDER_COLLECTOR,
    BALANCE_VIEW,
    SHOW_RATES_IN_DELIVERY_PAGE,
    ONE_SHOT_DELIVERY,
    LOAD_INFORMATION,
    MONEY_CALCULATOR,
    SMS_ORDERING,
    CUSTOMER_TRANSACTIONS,
    MONEY_DEPOSITS,
    WEB_PORTAL;

    companion object {
        fun getString(enum: ActivityAuthEnums): String? {
            return when (enum) {
                ADMIN -> "Dashboard"
                DELIVERY -> "Delivery"
                COLLECTOR -> "Collect Money"
                ORDER_COLLECTOR -> "Get Orders"
                BALANCE_VIEW -> "Customer Balances"
                ONE_SHOT_DELIVERY -> "1 Shot Delivery"
                LOAD_INFORMATION -> "Load Information"
                MONEY_CALCULATOR -> "Money Counter"
                SMS_ORDERING -> "SMS Orders"
                CUSTOMER_TRANSACTIONS -> "Customer Transactions"
                WEB_PORTAL -> "Web Portal"
                MONEY_DEPOSITS -> "Money Deposits"
                else -> null
            }
        }
    }
}