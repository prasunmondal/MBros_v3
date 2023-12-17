package com.tech4bytes.mbrosv3.BusinessData

import android.annotation.SuppressLint
import android.provider.Settings
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import kotlin.reflect.KMutableProperty1

data class SingleAttributedDataModel(
    var recordGeneratorDevice: String = "",
    var id: String = "",
    var datetime: String = "",
    var date: String = "",
    var openingFarmRate: String = "",
    var finalFarmRate: String = "",
    var bufferRate: String = "",
    var vehicle_prevKm: String = "",
    var vehicle_finalKm: String = "",
    var estimatedLoadPc: String = "",
    var estimatedLoadKg: String = "",
    var estimatedLoadAvgWt: String = "",
    var actualLoadPc: String = "",
    var actualLoadKg: String = "",
    var actualLoadAvgWt: String = "",
    var load_companyName: String = "",
    var load_branch: String = "",
    var load_area: String = "",
    var extra_cash_given: String = "",
    var load_account: String = "",
    var did_refueled: String = "",
    var refueling_qty: String = "",
    var refueling_amount: String = "",
    var refueling_isFullTank: String = "",
    var refueling_prevKm: String = "",
    var refueling_km: String = "",
    var labour_expenses: String = "",
    var refuel_mileage: String = "",
    var extra_expenses: String = "",
    var daySale: String = "",
    var totalMarketDue: String = "",
    var smsOrderSequence: String = "",
    var numberOfPeopleTakingSalary: String = "",
    var salaryDivision: String = "",
) : java.io.Serializable