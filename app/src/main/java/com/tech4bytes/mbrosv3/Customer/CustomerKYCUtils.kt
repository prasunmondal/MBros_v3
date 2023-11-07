package com.tech4bytes.mbrosv3.Customer

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class CustomerKYCUtils : java.io.Serializable {
    companion object {
        fun showBalance(engName: String): Boolean {
            CustomerKYC.getAllCustomers().forEach {
                if (it.nameEng == engName)
                    return it.showDue.toBoolean()
            }
            return true
        }

        fun getCustomerByEngName(engName: String): CustomerKYCModel? {
            CustomerKYC.getAllCustomers().forEach {
                if (it.nameEng == engName)
                    return it
            }
            return null
        }
    }
}