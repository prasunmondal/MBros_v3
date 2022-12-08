package com.tech4bytes.mbrosv3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ActivityDeliveringLoad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_load)
    }

    fun goToDeliveringDeliverPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliver::class.java)
        startActivity(switchActivityIntent)
    }
}