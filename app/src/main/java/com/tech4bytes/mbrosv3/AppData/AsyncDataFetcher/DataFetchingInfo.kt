package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling
import kotlin.reflect.KFunction

class DataFetchingInfo {
    companion object {

        fun getDescription(executingMethod: KFunction<Any>): String {
            return when(executingMethod) {
                GetCustomerOrders::get -> "Get Customer Orders"
                else -> "Get Data"
            }
        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when(activity){
                ActivityAuthEnums.ONE_SHOT_DELIVERY -> {
                    executingMethods.add(GetCustomerOrders::get)
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(CustomerData::getRecords)
                    executingMethods.add(SingleAttributedData::getRecords)
                    executingMethods.add(DeliverCustomerOrders::get)
                    executingMethods.add(DaySummary::get)
                    executingMethods.add(Refueling::get)
                }
                ActivityAuthEnums.BALANCE_VIEW -> {

                }
                ActivityAuthEnums.ONE_SHOT_LOAD_DETAILS -> {
                    executingMethods.add(SingleAttributedData::getRecords)
                }
                else -> {}
            }
            return executingMethods
        }
    }
}