package com.tech4bytes.mbrosv3.Customer

import com.google.gson.reflect.TypeToken
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher.DataFetchingInfo
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
    var otherBalances: String = ""
) : java.io.Serializable

object CustomerKYC : GSheetSerialized<CustomerKYCModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURL,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "customerDetails",
    query = null,
    classTypeForResponseParsing = CustomerKYCModel::class.java,
    appendInServer = true,
    appendInLocal = true
) {

    fun getByName(englishName: String): CustomerKYCModel? {
        fetchAll().execute().forEach {
            if (it.nameEng == englishName)
                return it
        }
        return null
    }

    fun showBalance(engName: String): Boolean {
        fetchAll().execute().forEach {
            if (it.nameEng == engName)
                return it.showDue.toBoolean()
        }
        return true
    }

    fun getCustomerByEngName(engName: String): CustomerKYCModel? {
        fetchAll().execute().forEach {
            if (it.nameEng == engName)
                return it
        }
        return null
    }
}