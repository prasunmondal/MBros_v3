package com.tech4bytes.mbrosv3.Customer

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.OneShot.Delivery.ReferralType
import com.tech4bytes.mbrosv3.ProjectConfig

data class CustomerKYCModel(
    var date: String,
    var nameEng: String,
    var nameBeng: String,
    var phNo1: String,
    var phNo2: String,
    var address: String,
    var showDue: String,
    var rateDifference: String = "",
    var isActiveCustomer: String = "",
    var referredBy: String = "",
    var showBalanceView: Boolean,
    var smsNumber: String = "",
    var smsText: String = "",
    var referralType: ReferralType = ReferralType.NONE,
    var referralInput: String = "",
    var leftHandBalance: String = ""
) : java.io.Serializable

object CustomerKYC : Tech4BytesSerializable<CustomerKYCModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "customerDetails",
    object : TypeToken<ArrayList<CustomerKYCModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true) {

    fun getByName(englishName: String): CustomerKYCModel? {
        get().forEach {
            if (it.nameEng == englishName)
                return it
        }
        return null
    }

    fun showBalance(engName: String): Boolean {
        get().forEach {
            if (it.nameEng == engName)
                return it.showDue.toBoolean()
        }
        return true
    }

    fun getCustomerByEngName(engName: String): CustomerKYCModel? {
        get().forEach {
            if (it.nameEng == engName)
                return it
        }
        return null
    }
}