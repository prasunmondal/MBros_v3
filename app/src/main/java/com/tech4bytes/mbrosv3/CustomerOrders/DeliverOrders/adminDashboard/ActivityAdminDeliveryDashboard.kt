package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class ActivityAdminDeliveryDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_delivery_dashboard)
        AppContexts.set(this)
    }


}