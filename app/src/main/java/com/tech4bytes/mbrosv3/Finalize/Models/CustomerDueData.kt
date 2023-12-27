package com.tech4bytes.mbrosv3.Finalize.Models

import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Payments.Staged.StagedPaymentUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils


class CustomerDueData {
    companion object {

        fun getBalance(shouldIncludePostDeliveryUpdates: Boolean = true, includeStagedPayments: Boolean = true): MutableMap<String, Int> {
            val dueMap: MutableMap<String, Int> = mutableMapOf()
            CustomerDataUtils.getAllLatestRecordsByAccount().forEach {
                dueMap[it.customerAccount] = NumberUtils.getIntOrZero(it.balanceDue)
            }
            if (shouldIncludePostDeliveryUpdates) {
                DeliverToCustomerDataHandler.get().forEach {
                    dueMap[it.customerAccount] = NumberUtils.getIntOrZero(it.balanceDue)
                }
            }
            if (includeStagedPayments) {
                dueMap.forEach { (key, value) ->
                    dueMap[key] = value - NumberUtils.getIntOrZero(StagedPaymentUtils.getStagedPayments(key).paidAmount)
                }
            }
            return dueMap
        }

        fun getBalance(name: String, shouldIncludePostDeliveryUpdates: Boolean = true, includeStagedPayments: Boolean = true): Int {
            return getBalance(shouldIncludePostDeliveryUpdates, includeStagedPayments)[name] ?: 0
        }

        fun getLastFinalizedDue(name: String, useCache: Boolean = true): String {
            val customerRecords = CustomerDataUtils.getAllLatestRecords(useCache)
            customerRecords.forEach {
                if (it.customerAccount == name)
                    return it.balanceDue
            }
            return "0"
        }
    }
}