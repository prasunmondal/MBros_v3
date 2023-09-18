package com.tech4bytes.mbrosv3.Finalize.Models

import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.stream.Collectors
import java.util.stream.Collectors.toList




class CustomerDueData {
    companion object {

        fun getBalance(shouldIncludePostDeliveryUpdates: Boolean = true): MutableMap<String, Int> {
            val dueMap: MutableMap<String, Int> = mutableMapOf()
            CustomerData.getAllLatestRecords().forEach {
                dueMap[it.name] = NumberUtils.getIntOrZero(it.balanceDue)
            }
            if (shouldIncludePostDeliveryUpdates) {
                DeliverToCustomerDataHandler.get().forEach {
                    dueMap[it.name] = NumberUtils.getIntOrZero(it.balanceDue)
                }
            }
            return dueMap
        }

        fun getBalance(name: String, shouldIncludePostDeliveryUpdates: Boolean = true): Int {
            return getBalance(shouldIncludePostDeliveryUpdates)[name] ?: 0
        }
    }
}