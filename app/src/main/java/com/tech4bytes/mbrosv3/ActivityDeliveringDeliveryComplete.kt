package com.tech4bytes.mbrosv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ActivityDeliveringDeliveryComplete : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_delivery_complete)
    }

    fun closeApp(view: View) {
        this.finishAffinity()
    }
}