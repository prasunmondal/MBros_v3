package com.tech4bytes.mbrosv3.VehicleManagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.ActivityDeliveringDeliveryComplete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils

class ActivityRefueling : AppCompatActivity() {

    lateinit var mainContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refueling)
        AppContexts.set(this)
        AppUtils.logError(this)
        mainContainer = findViewById<ConstraintLayout>(R.id.activity_refueling_container)
        mainContainer.findViewById<EditText>(R.id.activity_refueling_km).setOnClickListener {}
    }

    fun getKM(): String {
        return UIUtils.getUIElementValue(mainContainer.findViewById<EditText>(R.id.activity_refueling_km))
    }

    fun getRefuelingAmount(): String {
        return UIUtils.getUIElementValue(mainContainer.findViewById<EditText>(R.id.activity_refueling_total_price))
    }

    fun getFuelMeasure(): String {
        return UIUtils.getUIElementValue(mainContainer.findViewById<EditText>(R.id.activity_refueling_oil_measure))
    }

    fun isTankFulled(): Boolean {
        return UIUtils.getUIElementValue(mainContainer.findViewById<Switch>(R.id.activity_refueling_isTankFulled)).toBoolean()
    }

    fun onClickRefuelingSubmitBtn(view: View) {
        val obj = RefuelingModel(measure = getFuelMeasure(), amount = getRefuelingAmount(), refueling_km = getKM(), is_full_tank = isTankFulled())
        RefuelingUtils.insert(obj).execute()
        goToActivityDeliveringDeliveryComplete()
    }

    private fun goToActivityDeliveringDeliveryComplete() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliveryComplete::class.java)
        startActivity(switchActivityIntent)
    }
}