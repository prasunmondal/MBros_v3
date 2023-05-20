package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils

class DeliveryCalculationUtils {
    companion object {

        fun getByName(inputName: String): DeliverCustomerOrders? {
            DeliverCustomerOrders.get().forEach {
                if (it.name == inputName) {
                    return it
                }
            }
            return null
        }

        fun getTotalPcDelivered(): Int {
            var sum = 0
            DeliverCustomerOrders.get().forEach {
                sum += NumberUtils.getIntOrZero(it.deliveredPc)
            }
            return sum
        }

        fun getTotalKgDelivered(): Double {
            var sum = 0.0
            DeliverCustomerOrders.get().forEach {
                sum += NumberUtils.getDoubleOrZero(it.deliveredKg)
            }
            return sum
        }

        fun filterToOnlyLatest(resultFromServer: List<DeliverCustomerOrders>): List<DeliverCustomerOrders> {
            val sorted = ListUtils.sortListByAttribute(resultFromServer, DeliverCustomerOrders::id).reversed()
            val map = mutableMapOf<String, DeliverCustomerOrders>()

            sorted.forEach {
                if (!map.containsKey(it.name)) {
                    map.put(it.name, it)
                }
            }
            return map.values.toList()
        }
    }
}