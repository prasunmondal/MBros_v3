package com.tech4bytes.mbrosv3.Login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.DueShow
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard.ActivityAdminDeliveryDashboard
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders.ActivityDeliveringListOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.ActivityGetCustomerOrders
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotDelivery
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ActivityLogin : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        AppContexts.set(this)
        AppUtils.logError()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        val roles = RolesUtils.getRoles()
        LogMe.log("Got Role: $roles")

        val container = findViewById<LinearLayout>(R.id.activity_login_roles_container)
        if(roles.size == 0) {
            logUnIdentifiedDevice()
        } else {
            if (roles.size == 1 && roles[0] == Roles.DELIVERY) {
                // if the user has only one role, directly go to the home page.
                goToHomePageAsPerRole(roles[0])
            } else {
                roles.forEach { role ->
                    if(getRoleAndActivityMapping(role)!=null) {
                        val layoutInflater = LayoutInflater.from(AppContexts.get())
                        val entry = layoutInflater.inflate(R.layout.fragment_activity_login_roles, null)

                        entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role).text = role.name

                        entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role).setOnClickListener {
                            goToHomePageAsPerRole(role)
                        }
                        container.addView(entry)
                    }
                }
            }
        }
    }

    private fun getRoleAndActivityMapping(role: Roles): (() -> Unit)? {
        if(role == Roles.UNIDENTIFIED) {
            showToastConnectToAdmin()
            return null
        }
        return when (role) {
            Roles.ADMIN -> ::goToAdminRole
            Roles.DELIVERY -> ::goToDeliveryRole
            Roles.COLLECTOR -> ::goToCollectorRole
            Roles.ORDER_COLLECTOR -> ::goToGetOrdersPage
            Roles.BALANCE_VIEW -> ::goToShowDues
            Roles.ONE_SHOT_DELIVERY -> ::goToOneShotDelivery
            Roles.SHOW_RATES_IN_DELIVERY_PAGE -> null
            else -> null
        }
    }

    private fun goToHomePageAsPerRole(role: Roles) {
        getRoleAndActivityMapping(role)?.invoke()
    }

    private fun goToOneShotDelivery() {
        val switchActivityIntent = Intent(this, OneShotDelivery::class.java)
        startActivity(switchActivityIntent)
    }

    private fun logUnIdentifiedDevice() {
        val time = DateUtils.getCurrentTimestamp()
        val id = System.currentTimeMillis().toString()

        Toast.makeText(this, "Registering Device: ${getPhoneId()}", Toast.LENGTH_LONG).show()
        val obj = RolesModel(id, time, getPhoneId(), Roles.UNIDENTIFIED.toString())
        PostObject.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.DB_SHEET_ID)
            .tabName(Config.SHEET_TAB_NAME)
            .dataObject(obj as Any)
            .build().execute()

        CentralCache.invalidateFullCache()
    }

    private fun goToCollectorRole() {
        val switchActivityIntent = Intent(this, CollectorVerifyMoneyCollectionActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun goToGetOrdersPage() {
        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
        startActivity(switchActivityIntent)
    }

    private fun goToDeliveryRole() {
//        val loadObj = LoadModel.get()
        goToDeliveringDeliverPage()
//        if(ActivityDeliveringLoad.isLoadingComplete(loadObj)) {
//            goToDeliveringDeliverPage()
//        } else {
//            val switchActivityIntent = Intent(this, ActivityDeliveringLoad::class.java)
//            startActivity(switchActivityIntent)
//        }
    }

    fun goToDeliveringDeliverPage() {
        val switchActivityIntent = Intent(this, ActivityDeliveringListOrders::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun goToAdminRole() {
        val switchActivityIntent = Intent(this, ActivityAdminDeliveryDashboard::class.java)
        startActivity(switchActivityIntent)
    }

    private fun goToShowDues() {
        val switchActivityIntent = Intent(this, DueShow::class.java)
        startActivity(switchActivityIntent)
    }

    fun showToastConnectToAdmin() {
        Toast.makeText(this, "Device Registration Pending. Connect Admin", Toast.LENGTH_LONG).show()
        CentralCache.invalidateFullCache()
    }

    private fun getPhoneId(): String {
        return Secure.getString(applicationContext.contentResolver,
            Secure.ANDROID_ID);
    }

    fun loginPageOnClickRefresh(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
    }
}
