package com.tech4bytes.mbrosv3.VehicleManagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.constraintlayout.widget.ConstraintLayout
import com.tech4bytes.mbrosv3.ActivityDeliveringDeliveryComplete
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class ActivityRefueling : AppCompatActivity() {

    lateinit var mainContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refueling)
        AppContexts.set(this, this)
        AppUtils.logError()

        mainContainer = findViewById<ConstraintLayout>(R.id.activity_refueling_container)

        mainContainer.findViewById<EditText>(R.id.activity_refueling_km).setOnClickListener {

        }
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
        val obj = Refueling(measure = getFuelMeasure(), amount = getRefuelingAmount(), refueling_km = getKM(), is_full_tank = isTankFulled())
        Refueling.addToServer(obj)
        goToActivityDeliveringDeliveryComplete()
    }

    private fun goToActivityDeliveringDeliveryComplete() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliveryComplete::class.java)
        startActivity(switchActivityIntent)
    }
}