package com.tech4bytes.mbrosv3.Loading

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tech4bytes.mbrosv3.Delivering.ActivityDeliveringDeliver
import com.tech4bytes.mbrosv3.R

class ActivityDeliveringLoad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_load)

        if(isLoadingComplete()) {
            goToDeliveringDeliverPage()
        }
    }

    private fun isLoadingComplete(): Boolean {
        return false
    }

    fun onClickDeliverButton(view: View) {
        goToDeliveringDeliverPage()
    }

    fun goToDeliveringDeliverPage() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliver::class.java)
        startActivity(switchActivityIntent)
    }
}