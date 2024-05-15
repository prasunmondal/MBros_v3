package com.tech4bytes.mbrosv3.BusinessLogic

import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerRecentData
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
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
            DeliverToCustomerDataHandler.get().forEach {
                sum += NumberUtils.getIntOrZero(it.deliverAmount)
            }
            return sum
        }

        fun getTotalOfPaidAmounts(): Int {
            var sum = 0
            DeliverToCustomerDataHandler.get().forEach {
                sum += NumberUtils.getIntOrZero(it.paid)
            }
            return sum
        }

        fun getTotalDeliveredPc(): Int {
            var sum = 0
            DeliverToCustomerDataHandler.get().forEach {
                sum += NumberUtils.getIntOrZero(it.deliveredPc)
            }
            return sum
        }

        fun getTotalDeliveredKg(): Double {
            var sum = 0.0
            DeliverToCustomerDataHandler.get().forEach {
                sum += NumberUtils.getDoubleOrZero(it.deliveredKg)
            }
            return sum
        }

        fun getKmCost(): Int {
            val currentKm = SingleAttributedDataUtils.getRecords().vehicle_finalKm
            return AppConstants.get(AppConstants.CAR_RATE_PER_KM).toInt() * getKmDiff(currentKm)
        }

        fun getKmCost(currentKm: String): Int {
            return AppConstants.get(AppConstants.CAR_RATE_PER_KM).toInt() * getKmDiff(currentKm)
        }

        fun getKmDiff(currentKm: String): Int {
            val currentKm = NumberUtils.getIntOrZero(currentKm)
            LogMe.log("Prev KM: " + DaySummaryUtils.getPrevTripEndKm())
            val prevKm = DaySummaryUtils.getPrevTripEndKm()
            return currentKm - prevKm
        }

        fun getTotalOtherExpenses(): Int {
            val metadata = SingleAttributedDataUtils.getRecords()
            val kmCost = getKmCost(metadata.vehicle_finalKm)
            val labourCost = NumberUtils.getIntOrZero(metadata.labour_expenses)
            val inHandCashExpenses = NumberUtils.getIntOrZero(metadata.extra_expenses)
            return kmCost + labourCost + inHandCashExpenses
        }

        fun getCumilativeKhataDue(): Int {
            val dueMap: MutableMap<String, Int> = mutableMapOf()
            CustomerRecentData.getAllLatestRecords().forEach {
                dueMap[it.name] = NumberUtils.getIntOrZero(it.khataBalance)
            }
            DeliverToCustomerDataHandler.get().forEach {
                dueMap[it.name] = NumberUtils.getIntOrZero(it.khataBalance)
            }

            var sum = 0
            dueMap.forEach {
                sum += it.value
            }
            return sum
        }

        fun getPrevCumilativeKhataDue(): Int {
            var sum = 0
            CustomerRecentData.getAllLatestRecords().forEach {
                sum += NumberUtils.getIntOrZero(it.khataBalance)
            }
            return sum
        }

        fun getBaseDeliveryPrice(farmRate: Int, buffer: Int): Int {
            return farmRate + buffer + NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DELIVERY_BASE_RATE_DIFF))
        }

        fun getBaseDeliveryPrice(farmRate: String, buffer: String): Int {
            return getBaseDeliveryPrice(NumberUtils.getIntOrZero(farmRate), NumberUtils.getIntOrZero(buffer))
        }


        fun getBufferPrice(farmRate: Int, deliveryRate: Int): Int {
            return deliveryRate - farmRate - NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DELIVERY_BASE_RATE_DIFF))
        }

        fun getBufferPrice(farmRate: String, deliveryRate: String): Int {
            return getBufferPrice(NumberUtils.getIntOrZero(farmRate), NumberUtils.getIntOrZero(deliveryRate))
        }
    }
}