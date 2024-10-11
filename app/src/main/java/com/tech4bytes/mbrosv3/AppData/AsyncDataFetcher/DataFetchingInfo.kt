package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

//import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrderUtils
import com.prasunmondal.dev.libs.gsheet.clients.GScript
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstantsUtil
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.SMSOrderModelUtil
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerRecentData
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OSMS
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.VehicleManagement.RefuelingUtils
import kotlin.reflect.KFunction

class DataFetchingInfo {
    companion object {

        fun getDescription(executingMethod: KFunction<Any>): String {
            LogMe.log(executingMethod.toString())
            return when (executingMethod.toString()) {
                SMSOrderModelUtil::fetchAll.toString() -> "Customer orders"
                CustomerKYC::fetchAll.toString() -> "Get Customer KYCs"
                CustomerRecentData::fetchAll.toString() -> "Getting Previous Recents"
                CustomerDataUtils::fetchAll.toString() -> "Previous delivery reports"
                SingleAttributedDataUtils::fetchAll.toString() -> "Metadata"
                DeliverToCustomerDataHandler::fetchAll.toString() -> "Current delivery reports"
                DaySummaryUtils::fetchAll.toString() -> "Transaction reports"
                RefuelingUtils::fetchAll.toString() -> "Fuel data"
                AppConstantsUtil::fetchAll.toString() -> "App Constants Data"
                OSMS::fetchAll.toString() -> "Fetch Message Templates"
                GScript::execute.toString() -> "Making the DB Call"
                else -> "Get data"
            }
        }

        fun get(activity: ActivityAuthEnums): ExecutingMethods {
            val executingMethods = ExecutingMethods()

            when (activity) {
                ActivityAuthEnums.ADMIN,
                ActivityAuthEnums.ONE_SHOT_DELIVERY,
                -> {
                    executingMethods.add(SingleAttributedDataUtils::fetchAll, {SingleAttributedDataUtils.fetchAll().queue() })
                    executingMethods.add(CustomerKYC::fetchAll, { CustomerKYC.fetchAll().queue() })
                    executingMethods.add(SMSOrderModelUtil::fetchAll, { SMSOrderModelUtil.fetchAll().queue() })
                    executingMethods.add(CustomerRecentData::fetchAll, {CustomerRecentData.fetchAll().queue() })
                    executingMethods.add(DeliverToCustomerDataHandler::fetchAll, {DeliverToCustomerDataHandler.fetchAll().queue() })
                    executingMethods.add(DaySummaryUtils::fetchAll, {DaySummaryUtils.fetchAll().queue()})
                    executingMethods.add(RefuelingUtils::fetchAll, {RefuelingUtils.fetchAll().queue() })
                    executingMethods.add(AppConstantsUtil::fetchAll, {AppConstantsUtil.fetchAll().queue() })
                }
                ActivityAuthEnums.DELIVERY -> {
                    executingMethods.add(CustomerKYC::fetchAll, {CustomerKYC.fetchAll().queue() })
                    executingMethods.add(SMSOrderModelUtil::fetchAll, { SMSOrderModelUtil.fetchAll().queue() })
                    executingMethods.add(DeliverToCustomerDataHandler::fetchAll, {DeliverToCustomerDataHandler.fetchAll().queue() })
                    executingMethods.add(CustomerRecentData::fetchAll, {CustomerRecentData.fetchAll().queue() })
                }
                ActivityAuthEnums.BALANCE_VIEW -> {
                    executingMethods.add(CustomerKYC::fetchAll, {CustomerKYC.fetchAll().queue() })
                    executingMethods.add(CustomerRecentData::fetchAll, {CustomerRecentData.fetchAll().queue() })
                    executingMethods.add(DeliverToCustomerDataHandler::fetchAll, {DeliverToCustomerDataHandler.fetchAll().queue() })
                }
                ActivityAuthEnums.ORDER_COLLECTOR -> {
                    executingMethods.add(SingleAttributedDataUtils::fetchAll, {SingleAttributedDataUtils.fetchAll().queue() })
                    executingMethods.add(CustomerKYC::fetchAll, {CustomerKYC.fetchAll().queue() })
                    executingMethods.add(SMSOrderModelUtil::fetchAll, { SMSOrderModelUtil.fetchAll().queue() })
                    executingMethods.add(CustomerRecentData::fetchAll, {CustomerRecentData.fetchAll().queue() })
                }
                ActivityAuthEnums.LOAD_INFORMATION -> {
                    executingMethods.add(SingleAttributedDataUtils::fetchAll, {SingleAttributedDataUtils.fetchAll().queue() })
                    executingMethods.add(DaySummaryUtils::fetchAll, {DaySummaryUtils.fetchAll().queue()})
                }
                ActivityAuthEnums.MONEY_CALCULATOR -> {
                    executingMethods.add(SingleAttributedDataUtils::fetchAll, {SingleAttributedDataUtils.fetchAll().queue() })
                    executingMethods.add(DeliverToCustomerDataHandler::fetchAll, {DeliverToCustomerDataHandler.fetchAll().queue() })
                    executingMethods.add(AppConstantsUtil::fetchAll, {AppConstantsUtil.fetchAll().queue() })
                }
                ActivityAuthEnums.SMS_ORDERING -> {
                    executingMethods.add(SingleAttributedDataUtils::fetchAll, {SingleAttributedDataUtils.fetchAll().queue() })
                    executingMethods.add(SMSOrderModelUtil::fetchAll, {SMSOrderModelUtil.fetchAll().queue() })
                    executingMethods.add(CustomerKYC::fetchAll, {CustomerKYC.fetchAll().queue() })
                    executingMethods.add(AppConstantsUtil::fetchAll, {AppConstantsUtil.fetchAll().queue() })
                    executingMethods.add(DeliverToCustomerDataHandler::fetchAll, {DeliverToCustomerDataHandler.fetchAll().queue() })
                    executingMethods.add(CustomerRecentData::fetchAll, {CustomerRecentData.fetchAll().queue() })
                }
                ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> {
                    executingMethods.add(CustomerDataUtils::fetchAll, {CustomerDataUtils.fetchAll().queue() })
                }
                ActivityAuthEnums.ADD_TRANSACTION -> {
                    executingMethods.add(SingleAttributedDataUtils::fetchAll, {SingleAttributedDataUtils.fetchAll().queue() })
                    executingMethods.add(CustomerRecentData::fetchAll, {CustomerRecentData.fetchAll().queue() })
                    executingMethods.add(CustomerDataUtils::fetchAll, {CustomerDataUtils.fetchAll().queue() })
                    executingMethods.add(DaySummaryUtils::fetchAll, {DaySummaryUtils.fetchAll().queue() })
                    executingMethods.add(DeliverToCustomerDataHandler::fetchAll, {DeliverToCustomerDataHandler.fetchAll().queue() })
                    executingMethods.add(OSMS::fetchAll, {OSMS.fetchAll().queue() })
                }
                else -> {}

            }
//            executingMethods.add(OSMS::fetchAll, { GScript.execute(ProjectConfig.dBServerScriptURLNew) })
            return executingMethods
        }
    }
}