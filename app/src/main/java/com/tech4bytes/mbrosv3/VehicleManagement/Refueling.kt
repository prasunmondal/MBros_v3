package com.tech4bytes.mbrosv3.VehicleManagement

import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

class Refueling: java.io.Serializable {

    var timestamp: String = ""
    var measure: String = ""
    var rate = 0.0
    var amount = ""
    var refueling_km = ""
    var is_full_tank = true
    var mileage = ""

    constructor(measure: String, amount: String, refueling_km: String, is_full_tank: Boolean) {
        this.timestamp = DateUtils.getCurrentTimestamp()
        this.measure = measure
        this.amount = amount
        this.refueling_km = refueling_km
        this.is_full_tank = is_full_tank
        this.rate = try {
            amount.toDouble() / measure.toDouble()
        } catch (e: Exception) {
            0.0
        }
        this.mileage = ""
    }

    companion object {

        fun addToServer(refuelDetailsObj: Refueling) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(RefuelingConfig.SHEET_REFUELING_TAB_NAME)
                .dataObject(refuelDetailsObj as Any)
                .build().execute()
        }
    }
}