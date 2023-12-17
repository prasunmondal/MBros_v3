package com.tech4bytes.mbrosv3.VehicleManagement

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class RefuelingModel : java.io.Serializable {

    var id: String = ""
    var timestamp: String = ""
    var measure: String = ""
    var rate = ""
    var amount = ""
    var prev_refuel_km = ""
    var refueling_km = ""
    var is_full_tank = true
    var mileage = ""

    constructor(measure: String, amount: String, refueling_km: String, is_full_tank: Boolean) {
        this.timestamp = DateUtils.getCurrentTimestamp()
        this.measure = measure
        this.amount = amount
        this.refueling_km = refueling_km
        this.is_full_tank = is_full_tank
        this.mileage = ""
    }
}