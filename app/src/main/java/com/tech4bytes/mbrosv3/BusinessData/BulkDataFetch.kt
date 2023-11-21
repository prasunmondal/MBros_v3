package com.tech4bytes.mbrosv3.BusinessData

import com.prasunmondal.postjsontosheets.clients.get.GetMultipleTabs
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OSMS
import kotlin.reflect.KFunction

class BulkDataFetch {

    companion object {
        fun getAllData(useCache: Boolean = true) {
            val sheetClassMap: MutableMap<String, KFunction<Any>> = mutableMapOf()
            sheetClassMap["smsModel"] = OSMS::parseAndSaveToLocal
            sheetClassMap["metadata"] = SingleAttributedData::parseAndSaveToLocal
            sheetClassMap["customerDetails"] = CustomerKYC::parseAndSaveToLocal
//            sheetClassMap["deliveries"] = CustomerData::parseAndSaveToLocal
            sheetClassMap["DeliverOrders"] = DeliverToCustomerDataHandler::parseAndSaveToLocal


            GetMultipleTabs.builder().scriptId("https://script.google.com/macros/s/AKfycbyVdzZW7Bg5-tAFM4_LfWfBozea-OPyFQQrHMGkNJiqXBsEyMZXNlG-QbX5aF5VXABPZQ/exec")
                .sheetId("1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w")
                .tabName("smsModel,metadata")
                .SheetClassMapBuilder(sheetClassMap)
                .build()
                .execute()
//            return listOf("")
        }
    }
}