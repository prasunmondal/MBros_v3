package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class ActivityDeliveringDeliver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_deliver)
        AppContexts.set(this, this)

        val inputName = intent.extras!!.get("name")
        LogMe.log("Delivering to: $inputName")
    }

    fun getDataFromDeliveringCache() {

    }
}