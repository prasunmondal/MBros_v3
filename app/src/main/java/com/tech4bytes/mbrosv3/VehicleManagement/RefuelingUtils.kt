package com.tech4bytes.mbrosv3.VehicleManagement

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

object RefuelingUtils : Tech4BytesSerializable(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "fuel",
    appendInServer = true,
    appendInLocal = true,
    cacheObjectType = object : TypeToken<ArrayList<RefuelingModel?>?>() {}.type
)
{
        private fun getLatestRecord(useCache: Boolean = true): RefuelingModel {
            val list = get<RefuelingModel>(useCache)
            return list[list.size - 1]
        }

        fun getPreviousRefuelingKM(): String {
            return getLatestRecord().refueling_km
        }

        fun getKmDifferenceForRefueling(currentKM: String): Int {
            LogMe.log("Getting km difference: ")
            LogMe.log("CurrentKM: " + currentKM + " : " + NumberUtils.getIntOrZero(currentKM))
            LogMe.log("PrevKM: " + getPreviousRefuelingKM() + " : " + NumberUtils.getIntOrZero(getPreviousRefuelingKM()))
            LogMe.log("Result: " + (NumberUtils.getIntOrZero(currentKM) - NumberUtils.getIntOrZero(getPreviousRefuelingKM())))

            return (NumberUtils.getIntOrZero(currentKM) - NumberUtils.getIntOrZero(getPreviousRefuelingKM()))
        }

        fun getMileage(currentKm: String, oilQuantity: String): String {
            val kmDiff = getKmDifferenceForRefueling(currentKm)
            val oilQty = NumberUtils.getDoubleOrZero(oilQuantity)
            val mileage: Double = kmDiff / oilQty
            val rounded = NumberUtils.roundDownDecimal3Places(mileage)

            return "$rounded"
        }

        fun spoolRefuelingData() {
            val singleAttributedObj = SingleAttributedDataUtils.getRecords()
            if (singleAttributedObj.did_refueled.toBoolean()) {
                val refuelingObj = RefuelingModel("", "", "", false)
                refuelingObj.id = singleAttributedObj.id
                refuelingObj.timestamp = singleAttributedObj.datetime
                refuelingObj.measure = singleAttributedObj.refueling_qty
                refuelingObj.amount = singleAttributedObj.refueling_amount
                refuelingObj.refueling_km = singleAttributedObj.refueling_km
                refuelingObj.is_full_tank = singleAttributedObj.refueling_isFullTank.toBoolean()
                refuelingObj.prev_refuel_km = singleAttributedObj.refueling_prevKm
                val calculatedCate = NumberUtils.getDoubleOrZero(singleAttributedObj.refueling_amount) / NumberUtils.getDoubleOrZero(singleAttributedObj.refueling_qty)
                refuelingObj.rate = "%.2f".format(calculatedCate)

                val calculatedMileage = (NumberUtils.getIntOrZero(refuelingObj.refueling_km) - NumberUtils.getIntOrZero(refuelingObj.prev_refuel_km)) / NumberUtils.getDoubleOrZero(refuelingObj.measure)
                refuelingObj.mileage = "%.3f".format(calculatedMileage)
                saveToServer(refuelingObj)
            }
        }
}