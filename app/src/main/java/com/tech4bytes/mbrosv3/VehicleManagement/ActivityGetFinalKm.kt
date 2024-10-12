package com.tech4bytes.mbrosv3.VehicleManagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.ActivityDeliveringDeliveryComplete
import com.tech4bytes.mbrosv3.BusinessData.DayMetadata
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils

class ActivityGetFinalKm : AppCompatActivity() {

    lateinit var mainContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_final_km)
        AppContexts.set(this)

        mainContainer = findViewById<LinearLayout>(R.id.activity_get_final_km_container)
    }

    private fun getFinalKm(): Long {
        return UIUtils.getUIElementValue(mainContainer.findViewById(R.id.activity_get_final_km_value)).toLong()
    }

    fun onClickFinalKmSubmit(view: View) {
        val data = DayMetadata.getRecords()
        data.vehicle_finalKm = getFinalKm().toString()
        DayMetadata.insert(data).execute()
        DayMetadata.clearLocalObj()
        goToActivityDeliveringDeliveryComplete()
    }

    private fun goToActivityDeliveringDeliveryComplete() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliveryComplete::class.java)
        startActivity(switchActivityIntent)
    }
}