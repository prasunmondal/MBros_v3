package com.tech4bytes.mbrosv3

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.UserRoleUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders.ActivityDeliveringListOrders

class ActivityDeliveringDeliveryComplete : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_delivery_complete)
        AppContexts.set(this)
        AppUtils.logError(this)
    }

    fun closeApp(view: View) {
        view.isEnabled = false
        view.alpha = 0.5f
        view.isClickable = false
        if (UserRoleUtils.doesHaveRole(ActivityAuthEnums.COLLECTOR) || UserRoleUtils.doesHaveRole(ActivityAuthEnums.ADMIN))
            goToDeliveringListPage()
        else
            this.finishAffinity()
    }

    fun goToDeliveringListPage() {
        val switchActivityIntent = Intent(this, ActivityDeliveringListOrders::class.java)
        startActivity(switchActivityIntent)
        finish()
    }
}