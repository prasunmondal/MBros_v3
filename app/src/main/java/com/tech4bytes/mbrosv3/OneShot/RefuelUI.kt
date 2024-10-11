package com.tech4bytes.mbrosv3.OneShot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.clients.GScript
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Android.MeteredNumbers
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.VehicleManagement.RefuelingUtils

@SuppressLint("UseSwitchCompatOrMaterialCode")
class RefuelUI(
    private var context: Context,
    private var uiFinalKmContainer: LinearLayout,
    private var uiContainer: LinearLayout
) {
    private lateinit var meteredFuelKms: MeteredNumbers
    private lateinit var meteredKm: MeteredNumbers

    fun initiallizeRefuelUI() {
        val didRefuelElement =
            uiContainer.findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        val didTankFullElement =
            uiContainer.findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
        val refuelQtyElement =
            uiContainer.findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelAmountElement = uiContainer.findViewById<EditText>(R.id.osd_refuel_amount)

        didRefuelElement.setOnCheckedChangeListener { _, isChecked ->
            val obj = SingleAttributedDataUtils.getRecords()
            obj.did_refueled = isChecked.toString()
            SingleAttributedDataUtils.saveToLocal(obj)
            updateRefuelingUIDetails()
        }

        didTankFullElement.setOnCheckedChangeListener { _, isChecked ->
            val obj = SingleAttributedDataUtils.getRecords()
            obj.refueling_isFullTank = isChecked.toString()
            SingleAttributedDataUtils.saveToLocal(obj)
            updateRefuelingUIDetails()
        }

        val refuelingKmElementPart1 =
            uiContainer.findViewById<EditText>(R.id.one_shot_delivery_refueling_km_part1)
        val refuelingKmElementPart2 =
            uiContainer.findViewById<EditText>(R.id.one_shot_delivery_refueling_km_part2)
        meteredFuelKms = MeteredNumbers(refuelingKmElementPart1, refuelingKmElementPart2, 3)
        meteredFuelKms.setListeners { updateRefuelingUIDetails() }

        (context as Activity).runOnUiThread {
            UIUtils.setUIElementValue(
                didRefuelElement,
                SingleAttributedDataUtils.getRecords().did_refueled
            )
            UIUtils.setUIElementValue(
                didTankFullElement,
                SingleAttributedDataUtils.getRecords().refueling_isFullTank
            )
            UIUtils.setUIElementValue(
                refuelQtyElement,
                SingleAttributedDataUtils.getRecords().refueling_qty
            )
            UIUtils.setUIElementValue(
                refuelAmountElement,
                SingleAttributedDataUtils.getRecords().refueling_amount
            )
            meteredFuelKms.setNumber(
                context as Activity,
                NumberUtils.getIntOrZero(RefuelingUtils.getPreviousRefuelingKM()),
                true
            )
        }

        refuelQtyElement.doOnTextChanged { text, start, before, count ->
            updateRefuelingUIDetails()
            updateFuelRate()
        }
        refuelAmountElement.doOnTextChanged { text, start, before, count ->
            updateFuelRate()
        }
        updateRefuelingUIDetails()
    }

    fun updateRefuelingUIDetails() {
        if (meteredFuelKms.getNumber() != null) {
            val mileageLabel =
                uiContainer.findViewById<TextView>(R.id.one_shot_delivery_refueling_mileage)
            val refuelingKmDiffLabel =
                uiContainer.findViewById<TextView>(R.id.one_shot_delivery_refueling_km_diff)
            val refuelingDetailsContainer =
                uiContainer.findViewById<LinearLayout>(R.id.osd_refuel_container)
            val refuelingKmContainer =
                uiContainer.findViewById<LinearLayout>(R.id.one_shot_delivery_refueling_km_container)
            val didTankFullElement =
                uiContainer.findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
            val didRefuelElement =
                uiContainer.findViewById<Switch>(R.id.one_shot_delivery_did_refuel)

            (context as Activity).runOnUiThread {
                refuelingDetailsContainer.visibility =
                    if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
                didTankFullElement.visibility =
                    if (didRefuelElement.isChecked) View.VISIBLE else View.GONE
                refuelingKmContainer.visibility =
                    if (didTankFullElement.isChecked) View.VISIBLE else View.GONE

                refuelingKmDiffLabel.text =
                    if (didTankFullElement.isChecked)
                        RefuelingUtils.getKmDifferenceForRefueling(meteredFuelKms.getNumber()!!)
                            .toString()
                    else "N/A"
                mileageLabel.text =
                    if (didTankFullElement.isChecked) getMileage(uiContainer) + " km/L" else "N/A"
            }
            LogMe.log("KM: " + meteredFuelKms.getNumber())
            LogMe.log("Mileage: " + getMileage(uiContainer))

            if (RefuelingUtils.getKmDifferenceForRefueling(meteredFuelKms.getNumber()!!) > 0) {
                // add general kms from petrol pump to home, and set the total kms accordingly
                val refuelingKm = meteredFuelKms.getNumber()!!
                val addKmToFuelKmToGetFinalKm =
                    NumberUtils.getIntOrZero(AppConstants.get(AppConstants.ADD_TO_FUELING_KMS_TO_GET_FINAL_KM))
                meteredKm.setNumber(context as Activity, refuelingKm + addKmToFuelKmToGetFinalKm, false)
            }
        }
    }

    fun getRefuelingKms(): Int {
        return meteredFuelKms.getNumber()!!
    }

    fun getMileage(uiContainer: LinearLayout): String {
        val refuelQtyElement =
            uiContainer.findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)

        val refuelingKM = meteredFuelKms.getNumber()!!
        val refuelingQty = refuelQtyElement.text.toString()
        LogMe.log("Converting String: " + RefuelingUtils.getMileage(refuelingKM, refuelingQty))
        return if (NumberUtils.getDoubleOrZero(refuelingQty) > 0.0)
            RefuelingUtils.getMileage(refuelingKM, refuelingQty)
        else
            "N/A"
    }

    @SuppressLint("SetTextI18n")
    fun updateFuelRate() {
        val oilRateLabel =
            uiContainer.findViewById<TextView>(R.id.one_shot_delivery_refueling_oil_rate_per_litre)
        val fuelRate = getFuelRate(uiContainer)
        oilRateLabel.text = " ₹ $fuelRate"

        // Show red background if oil rate is not within limits
        val upperRateLimit =
            NumberUtils.getIntOrZero(AppConstants.get(AppConstants.FUEL_OIL_RATE_UPPER_LIMIT))
        val lowerRateLimit =
            NumberUtils.getIntOrZero(AppConstants.get(AppConstants.FUEL_OIL_RATE_LOWER_LIMIT))
        val fuelRateInInt = NumberUtils.getIntOrZero(fuelRate.toInt().toString())
        if (fuelRateInInt < lowerRateLimit || fuelRateInInt > upperRateLimit) {
            oilRateLabel.setBackgroundColor(
                ContextCompat.getColor(
                    AppContexts.get(),
                    R.color.osd_fuel_rate_not_matching
                )
            )
            oilRateLabel.setTextColor(ContextCompat.getColor(AppContexts.get(), R.color.white))
        } else {
            oilRateLabel.setBackgroundColor(0x00000000)
            oilRateLabel.setTextColor(
                ContextCompat.getColor(
                    AppContexts.get(),
                    R.color.osd_fuel_non_interactive_ok_text_color
                )
            )
        }
    }

    fun getFuelRate(uiContainer: LinearLayout): Double {
        val refuelAmountElement = uiContainer.findViewById<EditText>(R.id.osd_refuel_amount)
        val refuelQtyElement =
            uiContainer.findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
        val refuelingQty = NumberUtils.getDoubleOrZero(refuelQtyElement.text.toString())
        val refuelingAmount = NumberUtils.getDoubleOrZero(refuelAmountElement.text.toString())
        val fuelPrice = refuelingAmount / refuelingQty
        if (refuelingQty == 0.0)
            return 0.0
        return NumberUtils.roundOff2places(fuelPrice)
    }

    // kms
    fun initializeFinalKm() {
        val singleAttributedDataUtils = SingleAttributedDataUtils.getRecords()
        val salaryPaidElement = uiFinalKmContainer.findViewById<EditText>(R.id.osd_salary_paid)
        val extraExpensesElement =
            uiFinalKmContainer.findViewById<EditText>(R.id.one_shot_delivery_extra_expenses)
        val finalKmElementFirstPart =
            uiFinalKmContainer.findViewById<EditText>(R.id.one_shot_delivery_trip_end_km_first_part)
        val finalKmElementSecondPart =
            uiFinalKmContainer.findViewById<EditText>(R.id.one_shot_delivery_trip_end_km_second_part)
        meteredKm = MeteredNumbers(finalKmElementFirstPart, finalKmElementSecondPart, 3)

        val salaryDivisionElement =
            uiFinalKmContainer.findViewById<TextView>(R.id.osd_salary_division)
        val vehiclePrevKm: Int =
            NumberUtils.getIntOrZero(singleAttributedDataUtils.vehicle_finalKm)
        val policeBreakdown = uiFinalKmContainer.findViewById<EditText>(R.id.osd_police_breakdown)

        meteredKm.setNumber(context as Activity, vehiclePrevKm, true)
        meteredKm.setListeners {
            updateKmRelatedCosts(context as Activity, uiFinalKmContainer)
        }

        val salaryPaid =
            NumberUtils.getIntOrZero(singleAttributedDataUtils.labour_expenses) + NumberUtils.getIntOrZero(
                AppConstants.get(AppConstants.DRIVER_SALARY)
            )

        (context as Activity).runOnUiThread {
            salaryPaidElement.hint = SingleAttributedDataUtils.getEstimatedSalary().toString()
            if(SingleAttributedDataUtils.getEstimatedSalary() != 0) {
                UIUtils.setUIElementValue(salaryPaidElement, salaryPaid.toString())
            }
            UIUtils.setUIElementValue(
                extraExpensesElement,
                singleAttributedDataUtils.extra_expenses
            )
            UIUtils.setUIElementValue(
                salaryDivisionElement,
                singleAttributedDataUtils.salaryDivision.replace("#", "  #  ")
            )
            UIUtils.setUIElementValue(policeBreakdown, singleAttributedDataUtils.police_breakdown)
        }
    }

    fun saveFuelData() {
        val obj = SingleAttributedDataUtils.getRecords()
        val didRefuelElement = uiContainer.findViewById<Switch>(R.id.one_shot_delivery_did_refuel)
        obj.refueling_km = ""
        obj.refueling_prevKm = ""
        obj.refuel_mileage = ""
        obj.refueling_amount = ""
        obj.refueling_qty = ""
        obj.refueling_amount = ""
        obj.did_refueled = didRefuelElement.isChecked.toString()

        if (didRefuelElement.isChecked) {
            val didTankFullElement = uiContainer.findViewById<Switch>(R.id.one_shot_delivery_did_fuel_upto_tank_full)
            val refuelQtyElement = uiContainer.findViewById<EditText>(R.id.one_shot_delivery_fuel_quantity)
            val refuelAmountElement = uiContainer.findViewById<EditText>(R.id.osd_refuel_amount)

            obj.did_refueled = didRefuelElement.isChecked.toString()
            obj.refueling_isFullTank = didTankFullElement.isChecked.toString()
            obj.refueling_qty = refuelQtyElement.text.toString()
            obj.refueling_amount = refuelAmountElement.text.toString()

            if (didTankFullElement.isChecked) {
                obj.refueling_km = meteredFuelKms.getNumber().toString()
                obj.refueling_prevKm = RefuelingUtils.getPreviousRefuelingKM()
                obj.refuel_mileage = getMileage(uiContainer)
            } else {
                obj.refueling_km = ""
                obj.refueling_prevKm = ""
                obj.refuel_mileage = ""
            }
        }
        SingleAttributedDataUtils.saveToLocal(obj)
    }

    fun getFinalKm(): String {
        return meteredKm.getNumber().toString()
    }

    private fun updateKmRelatedCosts(activity: Activity, uiFinalKmContainer: LinearLayout) {
        Thread {
            val prevKmElement = uiFinalKmContainer.findViewById<TextView>(R.id.osd_prev_km)

            // if second part is not 3 digits, nothing is processed
            val currentKmOnUI = meteredKm.getNumber().toString()

            val kmDiffElement = uiFinalKmContainer.findViewById<TextView>(R.id.osd_km_diff)
            val kmCostElement = uiFinalKmContainer.findViewById<TextView>(R.id.osd_km_cost)

            val currentKm = NumberUtils.getIntOrZero(currentKmOnUI)
            val prevKm = DaySummaryUtils.getPrevTripEndKm()
            val kmDiff = DeliveryCalculations.getKmDiff(currentKmOnUI)
            val kmCost = DeliveryCalculations.getKmCost(currentKmOnUI)

            val singleDataObj = SingleAttributedDataUtils.getRecords()
            singleDataObj.vehicle_finalKm = currentKm.toString()
            SingleAttributedDataUtils.saveToLocal(singleDataObj)

            activity.runOnUiThread {
                prevKmElement.text = prevKm.toString()
                if (currentKm < prevKm) {
                    kmDiffElement.text = "N/A"
                    kmCostElement.text = "N/A"
                } else {
                    kmDiffElement.text = kmDiff.toString()
                    kmCostElement.text = kmCost.toString()
                }
            }
        }.start()
    }

    fun getSalaryPaid(uiFinalKmContainer: LinearLayout): Int {
        val salaryPaidElement = uiFinalKmContainer.findViewById<EditText>(R.id.osd_salary_paid)
        return NumberUtils.getIntOrZero(UIUtils.getTextOrHint(salaryPaidElement))
    }

    fun getExtraExpenses(uiFinalKmContainer: LinearLayout): Int {
        val extraExpensesElement =
            uiFinalKmContainer.findViewById<EditText>(R.id.one_shot_delivery_extra_expenses)
        return NumberUtils.getIntOrZero(extraExpensesElement.text.toString())
    }

    fun saveKmData() {
        val obj = SingleAttributedDataUtils.getRecords()
        val salaryPaid = getSalaryPaid(uiFinalKmContainer) - NumberUtils.getIntOrZero(
            AppConstants.get(AppConstants.DRIVER_SALARY)
        )
        obj.vehicle_finalKm = getFinalKm()
        obj.labour_expenses = salaryPaid.toString()
        obj.extra_expenses = getExtraExpenses(uiFinalKmContainer).toString()
        obj.police_breakdown = uiFinalKmContainer.findViewById<EditText>(R.id.osd_police_breakdown).text.toString()

        var sum = 0
        obj.police_breakdown.split("-").forEach {
            sum += NumberUtils.getIntOrZero(it.trim())
        }
        obj.police = sum.toString()
        SingleAttributedDataUtils.saveToLocal(obj)
    }
    fun saveDataFromThisUI(saveToServer: Boolean = true) {
        saveKmData()
        saveFuelData()
        if(saveToServer) {
            SingleAttributedDataUtils.insert(SingleAttributedDataUtils.getRecords()).queue()
            SingleAttributedDataUtils.fetchAll().queue()
            GScript.execute()
        }
    }
}