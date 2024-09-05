package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import kotlin.reflect.KFunction

class DataFetchingInfo {
    companion object {

        fun getDescription(executingMethod: KFunction<Any>): String {
            LogMe.log(executingMethod.toString())
            return when (executingMethod.toString()) {
//                GetCustomerOrderUtils::get.toString() -> "Customer orders"
//                CustomerKYC::get.toString() -> "Get Customer KYCs"
//                CustomerRecentData::fetchAll().execute.toString() -> "Getting Previous Recents"
//                CustomerDataUtils::get.toString() -> "Previous delivery reports"
//                SingleAttributedDataUtils::fetchAll.toString() -> "Metadata"
//                DeliverToCustomerDataHandler::get.toString() -> "Current delivery reports"
//                DaySummaryUtils::get.toString() -> "Transaction reports"
//                RefuelingUtils::get.toString() -> "Fuel data"
//                AppConstantsUtil::get.toString() -> "App Constants Data"
//                OSMS::get.toString() -> "Fetch Message Templates"
                else -> "Get data"
            }
        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when (activity) {
                ActivityAuthEnums.ADMIN,
                ActivityAuthEnums.ONE_SHOT_DELIVERY,
                -> {
//                    executingMethods.add(CustomerKYC::get, { CustomerKYC.get() })
//                    executingMethods.add(GetCustomerOrderUtils::get, {GetCustomerOrderUtils.get(
//
//                    )
//                    })
//                    executingMethods.add(CustomerRecentData::fetchAll().execute, {CustomerRecentData.get() })
//                    executingMethods.add(DeliverToCustomerDataHandler::get, {DeliverToCustomerDataHandler.get(
//
//                    )
//                    })
//                    executingMethods.add(DaySummaryUtils::get, {DaySummaryUtils.get()})
//                    executingMethods.add(RefuelingUtils::get, {RefuelingUtils.get() })
//                    executingMethods.add(AppConstantsUtil::get, {AppConstantsUtil.get() })
                }
                ActivityAuthEnums.DELIVERY -> {
//                    executingMethods.add(CustomerKYC::get, {CustomerKYC.get() })
//                    executingMethods.add(GetCustomerOrderUtils::get, {GetCustomerOrderUtils.get(
//
//                    )
//                    })
//                    executingMethods.add(DeliverToCustomerDataHandler::get, {DeliverToCustomerDataHandler.get(
//
//                    )
//                    })
//                    executingMethods.add(CustomerRecentData::get, {CustomerRecentData.fetchAll().execute() })
                }
                ActivityAuthEnums.BALANCE_VIEW -> {
//                    executingMethods.add(CustomerKYC::get, {CustomerKYC.get() })
//                    executingMethods.add(CustomerRecentData::get, {CustomerRecentData.fetchAll().execute() })
//                    executingMethods.add(DeliverToCustomerDataHandler::get, {DeliverToCustomerDataHandler.get(
//
//                    )
//                    })
                }
                ActivityAuthEnums.ORDER_COLLECTOR -> {
//                    executingMethods.add(SingleAttributedDataUtils::getRecords, {SingleAttributedDataUtils.get(
//
//                    )
//                    })
//                    executingMethods.add(CustomerKYC::get, {CustomerKYC.get() })
//                    executingMethods.add(GetCustomerOrderUtils::get, {GetCustomerOrderUtils.get(
//
//                    )
//                    })
//                    executingMethods.add(CustomerRecentData::get, {CustomerRecentData.fetchAll().execute() })
                }
                ActivityAuthEnums.LOAD_INFORMATION -> {
//                    executingMethods.add(SingleAttributedDataUtils::getRecords, {SingleAttributedDataUtils.get(
//
//                    )
//                    })
//                    executingMethods.add(DaySummaryUtils::get, {DaySummaryUtils.get()})
                }
                ActivityAuthEnums.MONEY_CALCULATOR -> {
//                    executingMethods.add(SingleAttributedDataUtils::getRecords, {SingleAttributedDataUtils.get(
//
//                    )
//                    })
//                    executingMethods.add(DeliverToCustomerDataHandler::get, {DeliverToCustomerDataHandler.get(
//
//                    )
//                    })
//                    executingMethods.add(AppConstants::preFetch, {AppConstantsUtil.get() })
                }
                ActivityAuthEnums.SMS_ORDERING -> {
//                    executingMethods.add(CustomerKYC::get, {CustomerKYC.get() })
//                    executingMethods.add(AppConstants::preFetch, {AppConstantsUtil.get() })
//                    executingMethods.add(DeliverToCustomerDataHandler::get, {DeliverToCustomerDataHandler.get(
//
//                    )
//                    })
//                    executingMethods.add(CustomerRecentData::get, {CustomerRecentData.fetchAll().execute() })
                }
                ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> {
//                    executingMethods.add(CustomerDataUtils::get, {CustomerDataUtils.get() })
                }
                ActivityAuthEnums.ADD_TRANSACTION -> {
//                    executingMethods.add(SingleAttributedDataUtils::get, {SingleAttributedDataUtils.get() })
//                    executingMethods.add(CustomerRecentData::get, {CustomerRecentData.fetchAll().execute() })
//                    executingMethods.add(CustomerDataUtils::get, {CustomerDataUtils.get() })
//                    executingMethods.add(DaySummaryUtils::get, {DaySummaryUtils.get() })
//                    executingMethods.add(DeliverToCustomerDataHandler::get, {DeliverToCustomerDataHandler.get() })
//                    executingMethods.add(OSMS::get, {OSMS.get() })
                }
                else -> {}
            }
            return executingMethods
        }
    }
}