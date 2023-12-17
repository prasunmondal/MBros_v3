package com.tech4bytes.mbrosv3.VehicleManagement

import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

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