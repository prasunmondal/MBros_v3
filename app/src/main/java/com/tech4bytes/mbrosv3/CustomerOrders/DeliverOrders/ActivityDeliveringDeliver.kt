package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class ActivityDeliveringDeliver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_deliver)
        AppContexts.set(this, this)

        showOrders()
    }

    fun showOrders() {
        var orders = GetCustomerOrders.get()
        orders.forEach {
            LogMe.log(it.toString())
        }
    }
}