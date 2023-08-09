package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
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
                GetCustomerOrders::get.toString() -> "Customer orders"
                CustomerKYC::getAllCustomers.toString() -> "Customer profile"
                CustomerData::getRecords.toString() -> "Previous delivery reports"
                SingleAttributedData::getRecords.toString() -> "Metadata"
                DeliverToCustomerDataHandler::get.toString() -> "Current delivery reports"
                DaySummary::get.toString() -> "Transaction reports"
                Refueling::get.toString() -> "Fuel data"
                AppConstants::fetchAll.toString() -> "App Constants Data"
                else -> "Get data"
            }
        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when (activity) {
                ActivityAuthEnums.ADMIN,
                ActivityAuthEnums.ONE_SHOT_DELIVERY -> {
                    executingMethods.add(SingleAttributedData::getRecords)
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(GetCustomerOrders::get)
                    executingMethods.add(CustomerData::getRecords)
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
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(CustomerData::getRecords)
                    executingMethods.add(DeliverToCustomerDataHandler::get)
                }
                ActivityAuthEnums.ORDER_COLLECTOR -> {
                    executingMethods.add(SingleAttributedData::getRecords)
                    executingMethods.add(CustomerKYC::getAllCustomers)
                    executingMethods.add(GetCustomerOrders::get)
                    executingMethods.add(CustomerData::getRecords)
                }
                ActivityAuthEnums.LOAD_INFORMATION -> {
                    executingMethods.add(SingleAttributedData::getRecords, false)
                    executingMethods.add(DaySummary::get, false)
                }
                ActivityAuthEnums.MONEY_CALCULATOR -> {
                    executingMethods.add(SingleAttributedData::getRecords)
                    executingMethods.add(DeliverToCustomerDataHandler::get)
                }
                ActivityAuthEnums.SMS_ORDERING -> {
                    executingMethods.add(AppConstants::fetchAll)
                    executingMethods.add(DeliverToCustomerDataHandler::get)
                    executingMethods.add(CustomerData::getRecords)
                }
                ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> {
                    executingMethods.add(CustomerData::getRecords)
                }
                else -> {}
            }
            return executingMethods
        }
    }
}