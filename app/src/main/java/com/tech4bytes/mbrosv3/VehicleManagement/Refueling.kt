package com.tech4bytes.mbrosv3.VehicleManagement

import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class Refueling: java.io.Serializable {

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

    companion object {

        fun addToServer(refuelDetailsObj: Refueling) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(VehicleManagementConfig.SHEET_REFUELING_TAB_NAME)
                .dataObject(refuelDetailsObj as Any)
                .build().execute()
        }

        fun getPreviousRefuelingKM(): String {
            // TODO: obj.refueling_km = <existing refueling km>
            return "0"
        }

        fun spoolRefuelingData() {
            val singleAttributedObj = SingleAttributedData.getRecords()
            val refuelingObj = Refueling("", "", "", false)
            refuelingObj.id = singleAttributedObj.id
            refuelingObj.timestamp = singleAttributedObj.date
            refuelingObj.measure = singleAttributedObj.refueling_qty
            refuelingObj.amount = singleAttributedObj.refueling_amount
            refuelingObj.refueling_km = singleAttributedObj.refueling_km
            refuelingObj.is_full_tank = singleAttributedObj.refueling_isFullTank.toBoolean()
            refuelingObj.prev_refuel_km = getPreviousRefuelingKM()
            val calculatedCate = NumberUtils.getDoubleOrZero(singleAttributedObj.refueling_amount) / NumberUtils.getDoubleOrZero(singleAttributedObj.refueling_qty)
            refuelingObj.rate = "%.2f".format(calculatedCate)

            val calculatedMileage = (NumberUtils.getIntOrZero(refuelingObj.refueling_km) - NumberUtils.getIntOrZero(refuelingObj.prev_refuel_km)) / NumberUtils.getDoubleOrZero(refuelingObj.measure)
            refuelingObj.mileage = "%.3f".format(calculatedMileage)
            addToServer(refuelingObj)
        }
    }
}