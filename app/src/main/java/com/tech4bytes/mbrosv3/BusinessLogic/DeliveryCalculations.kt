package com.tech4bytes.mbrosv3.BusinessLogic

import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.WeightUtils.WeightUtils

class DeliveryCalculations {
    companion object {

        fun getShortage(loadedKg: Double, deliveredKg: Double): Double {
            val shortage = (loadedKg - deliveredKg) * 100 / loadedKg
            return WeightUtils.roundOff3places(shortage)
        }

        fun getShortage(loadedKg: String, deliveredKg: String): Double {
            val shortage = (NumberUtils.getDoubleOrZero(loadedKg) - NumberUtils.getDoubleOrZero(deliveredKg)) * 100 / NumberUtils.getDoubleOrZero(loadedKg)
            return WeightUtils.roundOff3places(shortage)
        }

        fun getDaySaleAmount(): Int {
            var sum = 0
            DeliverCustomerOrders.get().forEach {
                sum += NumberUtils.getIntOrZero(it.todaysAmount)
            }
            return sum
        }

        fun getTotalOfPaidAmounts(): Int {
            var sum = 0
            DeliverCustomerOrders.get().forEach {
                sum += NumberUtils.getIntOrZero(it.paid)
            }
            return sum
        }

        fun getTotalDeliveredPc(): Int {
            var sum = 0
            DeliverCustomerOrders.get().forEach {
                sum += NumberUtils.getIntOrZero(it.deliveredPc)
            }
            return sum
        }

        fun getTotalDeliveredKg(): Double {
            var sum = 0.0
            DeliverCustomerOrders.get().forEach {
                sum += NumberUtils.getDoubleOrZero(it.deliveredKg)
            }
            return sum
        }

        fun getKmCost(): Int {
            val currentKm = SingleAttributedData.getRecords().vehicle_finalKm
            return 12 * getKmDiff(currentKm)
        }

        fun getKmCost(currentKm: String): Int {
            return 12 * getKmDiff(currentKm)
        }

        fun getKmDiff(currentKm: String): Int {
            val currentKm = NumberUtils.getIntOrZero(currentKm)
            LogMe.log("Prev KM: " + DaySummary.getPrevTripEndKm())
            val prevKm = DaySummary.getPrevTripEndKm()
            return currentKm - prevKm
        }

        fun getTotalOtherExpenses(): Int {
            val metadata = SingleAttributedData.getRecords()
            val kmCost = getKmCost(metadata.vehicle_finalKm)
            val labourCost = NumberUtils.getIntOrZero(metadata.labour_expenses)
            val inHandCashExpenses = NumberUtils.getIntOrZero(metadata.extra_expenses)
            return kmCost + labourCost + inHandCashExpenses
        }

        fun getTotalMarketDue(): Int {
            val dueMap: MutableMap<String, Int> = mutableMapOf()
            CustomerData.getAllLatestRecords().forEach {
                dueMap[it.name] = NumberUtils.getIntOrZero(it.balanceDue)
            }
            DeliverCustomerOrders.get().forEach {
                dueMap[it.name] = NumberUtils.getIntOrZero(it.balanceDue)
            }

            var sum = 0
            dueMap.forEach {
                sum += it.value
            }
            return sum
        }

        fun getPrevMarketDue(): Int {
            var sum = 0
            CustomerData.getAllLatestRecords().forEach {
                sum += NumberUtils.getIntOrZero(it.balanceDue)
            }
            return sum
        }
    }
}