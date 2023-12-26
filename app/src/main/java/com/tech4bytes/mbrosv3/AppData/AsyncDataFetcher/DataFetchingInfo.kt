package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import kotlin.reflect.KFunction

class DataFetchingInfo {
    companion object {

        fun getDescription(executingMethod: KFunction<Any>): String {
            LogMe.log(executingMethod.toString())
            return when (executingMethod.toString()) {
//                GetCustomerOrderUtils::get.toString() -> "Customer orders"
                CustomerKYC::getByName.toString() -> "Get Customer KYCs"
//                CustomerData::get.toString() -> "Previous delivery reports"
                SingleAttributedDataUtils::getRecords.toString() -> "Metadata"
//                DeliverToCustomerDataHandler::get.toString() -> "Current delivery reports"
//                DaySummary::get.toString() -> "Transaction reports"
//                RefuelingUtils::get<RefuelingModel>.toString() -> "Fuel data"
                AppConstants::preFetch.toString() -> "App Constants Data"
                else -> "Get data"
            }
        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when (activity) {
                ActivityAuthEnums.ADMIN,
                ActivityAuthEnums.ONE_SHOT_DELIVERY,
                -> {
                    executingMethods.add(SingleAttributedDataUtils::getRecords)
//                    executingMethods.add(CustomerKYC::get)
//                    executingMethods.add(GetCustomerOrderUtils::get)
//                    executingMethods.add(CustomerData::getRecords)
////                    executingMethods.add(DeliverToCustomerDataHandler::get)
//                    executingMethods.add(DaySummary::get)
//                    executingMethods.add(RefuelingUtils::get)
                    executingMethods.add(AppConstants::preFetch)
                }
                ActivityAuthEnums.DELIVERY -> {
//                    executingMethods.add(CustomerKYC::get)
//                    executingMethods.add(GetCustomerOrderUtils::get)
//                    executingMethods.add(DeliverToCustomerDataHandler::get)
//                    executingMethods.add(CustomerData::getRecords)
                }
                ActivityAuthEnums.BALANCE_VIEW -> {
//                    executingMethods.add(CustomerKYC::get)
//                    executingMethods.add(CustomerData::getRecords)
//                    executingMethods.add(DeliverToCustomerDataHandler::get)
                }
                ActivityAuthEnums.ORDER_COLLECTOR -> {
                    executingMethods.add(SingleAttributedDataUtils::getRecords)
//                    executingMethods.add(CustomerKYC::get)
//                    executingMethods.add(GetCustomerOrderUtils::get)
//                    executingMethods.add(CustomerData::getRecords)
                }
                ActivityAuthEnums.LOAD_INFORMATION -> {
                    executingMethods.add(SingleAttributedDataUtils::getRecords, true)
//                    executingMethods.add(DaySummary::get, true)
                }
                ActivityAuthEnums.MONEY_CALCULATOR -> {
                    executingMethods.add(SingleAttributedDataUtils::getRecords)
//                    executingMethods.add(DeliverToCustomerDataHandler::get)
                    executingMethods.add(AppConstants::preFetch)
                }
                ActivityAuthEnums.SMS_ORDERING -> {
//                    executingMethods.add(CustomerKYC::get)
                    executingMethods.add(AppConstants::preFetch)
//                    executingMethods.add(DeliverToCustomerDataHandler::get)
//                    executingMethods.add(CustomerData::getRecords)
                }
                ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> {
//                    executingMethods.add(CustomerData::getRecords)
                }
                else -> {}
            }
            return executingMethods
        }
    }
}