package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstantsUtil
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrderUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.VehicleManagement.RefuelingUtils
import kotlin.reflect.KClass

class DataFetchingInfo {
    companion object {

        fun getDescription(executingMethod: KClass<Any>): String {
//            LogMe.log(executingMethod.toString())
            LogMe.log(executingMethod.java.name)
            return when (executingMethod.java.name) {
                GetCustomerOrderUtils.javaClass.name -> "Customer orders"
                CustomerKYC.javaClass.name -> "Get Customer KYCs"
                CustomerDataUtils.javaClass.name -> "Previous delivery reports"
                SingleAttributedDataUtils.javaClass.name -> "Metadata"
                DeliverToCustomerDataHandler.javaClass.name -> "Current delivery reports"
                DaySummaryUtils.javaClass.name -> "Transaction reports"
                RefuelingUtils.javaClass.name -> "Fuel data"
                AppConstantsUtil.javaClass.name -> "App Constants Data"
                else -> "Get data"
            }
        }

//        fun <T: Any> getData(clazz: KClass<T>): () -> List<SingleAttributedDataModel> {
//            return when(clazz) {
//                SingleAttributedDataUtils::class -> { { SingleAttributedDataUtils.get() } }
//                CustomerKYC::class -> getStudents()  as List<T>
//                else -> null
//            }
//        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when (activity) {
                ActivityAuthEnums.ADMIN,
                ActivityAuthEnums.ONE_SHOT_DELIVERY,
                -> {
                    executingMethods.add(SingleAttributedDataUtils::class as KClass<Any>)
                    executingMethods.add(CustomerKYC::class as KClass<Any>)
                    executingMethods.add(GetCustomerOrderUtils::class as KClass<Any>)
                    executingMethods.add(CustomerDataUtils::class as KClass<Any>)
                    executingMethods.add(DeliverToCustomerDataHandler::class as KClass<Any>)
                    executingMethods.add(DaySummaryUtils::class as KClass<Any>)
                    executingMethods.add(RefuelingUtils::class as KClass<Any>)
                    executingMethods.add(AppConstantsUtil::class as KClass<Any>)
                }
//                ActivityAuthEnums.DELIVERY -> {
//                    executingMethods.add({ CustomerKYC.get() })
//                    executingMethods.add({ GetCustomerOrderUtils.get() })
//                    executingMethods.add({ DeliverToCustomerDataHandler.get() })
//                    executingMethods.add({ CustomerDataUtils.get() })
//                }
//                ActivityAuthEnums.BALANCE_VIEW -> {
//                    executingMethods.add({ CustomerKYC.get() })
//                    executingMethods.add({ CustomerDataUtils.get() })
//                    executingMethods.add({ DeliverToCustomerDataHandler.get() })
//                }
//                ActivityAuthEnums.ORDER_COLLECTOR -> {
//                    executingMethods.add({ SingleAttributedDataUtils.get() })
//                    executingMethods.add({ CustomerKYC.get() })
//                    executingMethods.add({ GetCustomerOrderUtils.get() })
//                    executingMethods.add({ CustomerDataUtils.get() })
//                }
//                ActivityAuthEnums.LOAD_INFORMATION -> {
//                    executingMethods.add({ SingleAttributedDataUtils.get() })
//                    executingMethods.add({ DaySummaryUtils.get() })
//                }
//                ActivityAuthEnums.MONEY_CALCULATOR -> {
//                    executingMethods.add({ SingleAttributedDataUtils.get() })
//                    executingMethods.add({ DeliverToCustomerDataHandler.get() })
//                    executingMethods.add({ AppConstantsUtil.get() })
//                }
//                ActivityAuthEnums.SMS_ORDERING -> {
//                    executingMethods.add({ CustomerKYC.get() })
//                    executingMethods.add({ AppConstantsUtil.get() })
//                    executingMethods.add({ DeliverToCustomerDataHandler.get() })
//                    executingMethods.add({ CustomerDataUtils.get() })
//                }
//                ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> {
//                    executingMethods.add({ CustomerDataUtils.get() })
//                }
                else -> {}
            }
            return executingMethods
        }
    }
}