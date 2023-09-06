package com.tech4bytes.mbrosv3.Login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher.DataFetchActivity
import com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher.DataFetchingInfo
import com.tech4bytes.mbrosv3.AppUsers.AppUsersModel
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.UserRoleUtils
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.AppUsers.Config
import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.DueShow
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard.ActivityAdminDeliveryDashboard
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders.ActivityDeliveringListOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.ActivityGetCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.CustomerTransactions
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.SMSOrdering
import com.tech4bytes.mbrosv3.MoneyCounter.MoneyCounter
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotDelivery
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotLoad
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
        supportActionBar!!.hide()
        AppContexts.set(this)
        AppUtils.logError()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val roles = RolesUtils.getAppUser()
        LogMe.log("Got Role: $roles")
        updateWelcomeDetails()
        Thread {
            val container = findViewById<LinearLayout>(R.id.activity_login_roles_container)
            if (UserRoleUtils.getUserRoles().isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this, "Registering Device: ${getPhoneId()}", Toast.LENGTH_LONG).show()
                }
                logUnIdentifiedDevice()
            } else {
                AuthorizationUtils.getAllUserAuthorizations()
                if (UserRoleUtils.getUserRoles().size == 1) {
                    // if the user has only one role, directly go to the home page.
                    goToHomePageAsPerRole(UserRoleUtils.getUserRoles()[0])
                } else {
                    UserRoleUtils.getUserRoles().forEach { role ->
                        if (getRoleAndActivityMapping(role) != null) {
                            val layoutInflater = LayoutInflater.from(AppContexts.get())
                            val entry = layoutInflater.inflate(R.layout.fragment_activity_login_roles, null)

                            val displayText = if(ActivityAuthEnums.getString(role) == null) role.name else ActivityAuthEnums.getString(role)
                            entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role).text = displayText

                            entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role).setOnClickListener {
                                goToHomePageAsPerRole(role)
                            }
                            runOnUiThread {
                                container.addView(entry)
                            }
                        }
                    }
                }
            }
        }.start()
    }

    private fun updateWelcomeDetails() {
        findViewById<TextView>(R.id.welcome_date).text = DateUtils.getCurrentDate("dd")
        findViewById<TextView>(R.id.welcome_month).text = DateUtils.getCurrentDate("MMMM")
        findViewById<TextView>(R.id.welcome_year).text = DateUtils.getCurrentDate("yyyy")
    }

    private fun getRoleAndActivityMapping(role: ActivityAuthEnums): (() -> Unit)? {
        if (role == ActivityAuthEnums.UNIDENTIFIED) {
            showToastConnectToAdmin()
            return null
        }
        return when (role) {
            ActivityAuthEnums.ADMIN -> ::goToAdminRole
            ActivityAuthEnums.DELIVERY -> ::goToDeliveringDeliverPage
            ActivityAuthEnums.COLLECTOR -> ::goToCollectorRole
            ActivityAuthEnums.ORDER_COLLECTOR -> ::goToGetOrdersPage
            ActivityAuthEnums.BALANCE_VIEW -> ::goToShowDues
            ActivityAuthEnums.ONE_SHOT_DELIVERY -> ::goToOneShotDelivery
            ActivityAuthEnums.LOAD_INFORMATION -> ::goToOneShotLoadDetailsPage
            ActivityAuthEnums.MONEY_CALCULATOR -> ::goToMoneyCalculatorPage
            ActivityAuthEnums.SMS_ORDERING -> ::goToSmsOrderingActivity
            ActivityAuthEnums.SHOW_RATES_IN_DELIVERY_PAGE -> null
            ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> ::goToCustomerTransactions
            else -> null
        }
    }

    private fun goToMoneyCalculatorPage() {
        goToDataFetchActivity(ActivityAuthEnums.MONEY_CALCULATOR, MoneyCounter::class.java)
    }

    private fun goToSmsOrderingActivity() {
        goToDataFetchActivity(ActivityAuthEnums.SMS_ORDERING, SMSOrdering::class.java)
    }

    private fun goToOneShotLoadDetailsPage() {
        goToDataFetchActivity(ActivityAuthEnums.LOAD_INFORMATION, OneShotLoad::class.java)
    }

    private fun goToHomePageAsPerRole(role: ActivityAuthEnums) {
        getRoleAndActivityMapping(role)?.invoke()
    }

    private fun goToOneShotDelivery() {
        goToDataFetchActivity(ActivityAuthEnums.ONE_SHOT_DELIVERY, OneShotDelivery::class.java)
    }

    private fun logUnIdentifiedDevice() {
        val time = DateUtils.getCurrentTimestamp()
        val id = System.currentTimeMillis().toString()

        val obj = AppUsersModel(id, time, getPhoneId(), ActivityAuthEnums.UNIDENTIFIED.toString(), AuthorizationEnums.NONE.toString())
        PostObject.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.get_db_sheet_id())
            .tabName(Config.SHEET_TAB_NAME)
            .dataObject(obj as Any)
            .build().execute()

        CentralCache.invalidateFullCache()
    }

    private fun goToCollectorRole() {
        goToDataFetchActivity(ActivityAuthEnums.COLLECTOR, CollectorVerifyMoneyCollectionActivity::class.java)
    }

    private fun goToGetOrdersPage() {
        goToDataFetchActivity(ActivityAuthEnums.ORDER_COLLECTOR, ActivityGetCustomerOrders::class.java)
    }

    private fun goToDeliveringDeliverPage() {
        goToDataFetchActivity(ActivityAuthEnums.DELIVERY, ActivityDeliveringListOrders::class.java)
    }

    private fun goToAdminRole() {
        goToDataFetchActivity(ActivityAuthEnums.ADMIN, ActivityAdminDeliveryDashboard::class.java)
    }

    private fun goToCustomerTransactions() {
        goToDataFetchActivity(ActivityAuthEnums.CUSTOMER_TRANSACTIONS, CustomerTransactions::class.java)
    }

    private fun goToDataFetchActivity(currentActivity: ActivityAuthEnums, nextActivity: Class<*>) {
        var switchActivityIntent = Intent(this, DataFetchActivity::class.java)
        if (DataFetchingInfo.get(currentActivity).get().isEmpty()) {
            switchActivityIntent = Intent(this, nextActivity)
            startActivity(switchActivityIntent)
        } else {
            switchActivityIntent.putExtra("nextActivity", nextActivity)
            switchActivityIntent.putExtra("currentActivity", currentActivity)
            startActivity(switchActivityIntent)
        }
    }

    private fun goToShowDues() {
        goToDataFetchActivity(ActivityAuthEnums.BALANCE_VIEW, DueShow::class.java)
    }

    fun showToastConnectToAdmin() {
        Toast.makeText(this, "Device Registration Pending. Connect Admin", Toast.LENGTH_LONG).show()
        CentralCache.invalidateFullCache()
    }

    @SuppressLint("HardwareIds")
    private fun getPhoneId(): String {
        return Secure.getString(
            applicationContext.contentResolver,
            Secure.ANDROID_ID
        )
    }

    fun loginPageOnClickRefresh(view: View) {
        val refreshBtn = findViewById<Button>(R.id.menu_refreshBtn)
        refreshBtn.isEnabled = false
        refreshBtn.alpha = .5f
        refreshBtn.isClickable = false
        refreshBtn.text = "Refreshing... .. ."
        AppUtils.invalidateAllDataAndRestartApp()
    }
}
