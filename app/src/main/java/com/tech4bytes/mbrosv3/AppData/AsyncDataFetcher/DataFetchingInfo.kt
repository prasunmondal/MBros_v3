package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling
import kotlin.reflect.KFunction

class DataFetchingInfo {
    companion object {

        fun getDescription(executingMethod: KFunction<Any>): String {
            LogMe.log(executingMethod.toString())
            return when (executingMethod.toString()) {
                GetCustomerOrders::get.toString() -> "Get Customer Orders"
                CustomerKYC::getAllCustomers.toString() -> "Get Customer Profile Data"
                CustomerData::getRecords.toString() -> "Get Finalized Customer Data"
                SingleAttributedData::getRecords.toString() -> "Get Singleton Data"
                DeliverToCustomerDataHandler::get.toString() -> "Get today's delivery records"
                DaySummary::get.toString() -> "Get Day-wise Summary"
                Refueling::get.toString() -> "Get Refueling Data"
                else -> "Get Data"
            }
        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when (activity) {
                ActivityAuthEnums.ADMIN -> {
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(GetCustomerOrders::get)
                    executingMethods.add(SingleAttributedData::getRecords)
                    executingMethods.add(DeliverToCustomerDataHandler::get)
                }
                ActivityAuthEnums.ONE_SHOT_DELIVERY -> {
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(GetCustomerOrders::get)
                    executingMethods.add(CustomerData::getRecords)
                    executingMethods.add(SingleAttributedData::getRecords)
                    executingMethods.add(DeliverToCustomerDataHandler::get)
                    executingMethods.add(DaySummary::get)
                    executingMethods.add(Refueling::get)
                }
                ActivityAuthEnums.DELIVERY -> {
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(GetCustomerOrders::get)
                    executingMethods.add(DeliverToCustomerDataHandler::get)
                }
                ActivityAuthEnums.BALANCE_VIEW -> {

                }
                ActivityAuthEnums.ONE_SHOT_LOAD_DETAILS -> {
                    executingMethods.add(SingleAttributedData::getRecords, false)
                    executingMethods.add(DaySummary::get, false)
                }
                else -> {}
            }
            return executingMethods
        }
    }
}