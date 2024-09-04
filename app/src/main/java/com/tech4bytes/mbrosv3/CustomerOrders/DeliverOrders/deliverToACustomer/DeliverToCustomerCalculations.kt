package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils

class DeliverToCustomerCalculations {
    companion object {

        fun getByName(inputName: String): DeliverToCustomerDataModel? {
            DeliverToCustomerDataHandler.fetchAll().execute().forEach {
                if (it.name == inputName) {
                    return it
                }
            }
            return null
        }

        fun getTotalPcDelivered(): Int {
            var sum = 0
            DeliverToCustomerDataHandler.fetchAll().execute().forEach {
                sum += NumberUtils.getIntOrZero(it.deliveredPc)
            }
            return sum
        }

        fun getTotalKgDelivered(): Double {
            var sum = 0.0
            DeliverToCustomerDataHandler.fetchAll().execute().forEach {
                sum += NumberUtils.getDoubleOrZero(it.deliveredKg)
            }
            return sum
        }

        fun getTotalAmountPaidTodayByCustomers(): Int {
            var sum = 0
            DeliverToCustomerDataHandler.fetchAll().execute().forEach {
                sum += NumberUtils.getIntOrZero(it.paid)
            }
            return sum
        }

        fun getTotalAmountPaidInCashTodayByCustomers(): Int {
            var sum = 0
            DeliverToCustomerDataHandler.fetchAll().execute().forEach {
                sum += NumberUtils.getIntOrZero(it.paidCash)
            }
            return sum
        }

        fun getTotalAmountPaidOnlineTodayByCustomers(): Int {
            var sum = 0
            DeliverToCustomerDataHandler.fetchAll().execute().forEach {
                sum += NumberUtils.getIntOrZero(it.paidOnline)
            }
            return sum
        }

        fun filterToOnlyLatest(resultFromServer: List<DeliverToCustomerDataModel>): List<DeliverToCustomerDataModel> {
            val sorted = ListUtils.sortListByAttribute(resultFromServer, DeliverToCustomerDataModel::id).reversed()
            val map = mutableMapOf<String, DeliverToCustomerDataModel>()

            sorted.forEach {
                if (!map.containsKey(it.name)) {
                    map[it.name] = it
                }
            }
            return map.values.toList()
        }
    }
}