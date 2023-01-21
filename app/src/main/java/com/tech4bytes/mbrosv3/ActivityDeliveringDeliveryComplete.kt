package com.tech4bytes.mbrosv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class ActivityDeliveringDeliveryComplete : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_delivery_complete)
        AppContexts.set(this, this)
        AppUtils.logError()
    }

    fun closeApp(view: View) {
        this.finishAffinity()
    }
}