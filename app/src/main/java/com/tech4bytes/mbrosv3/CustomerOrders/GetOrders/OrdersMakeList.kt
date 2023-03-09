package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tech4bytes.mbrosv3.R

class OrdersMakeList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_make_list)
    }

    fun onClickGoToOrdersGetPage(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    fun onClickGoToOrdersFinalizePage(view: View) {
        val switchActivityIntent = Intent(this, GetOrdersFinalize::class.java)
        startActivity(switchActivityIntent)
        finish()
    }
}