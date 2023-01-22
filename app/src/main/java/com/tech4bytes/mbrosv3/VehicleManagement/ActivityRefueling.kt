package com.tech4bytes.mbrosv3.VehicleManagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class ActivityRefueling : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refueling)
        AppContexts.set(this, this)
        AppUtils.logError()


    }
}