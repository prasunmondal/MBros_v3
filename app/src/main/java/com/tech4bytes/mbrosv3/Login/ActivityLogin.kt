package com.tech4bytes.mbrosv3.Login

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.DueShow
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard.ActivityAdminDeliveryDashboard
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders.ActivityDeliveringListOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.ActivityGetCustomerOrders
import com.tech4bytes.mbrosv3.Loading.ActivityDeliveringLoad
import com.tech4bytes.mbrosv3.Loading.LoadConfig
import com.tech4bytes.mbrosv3.Loading.LoadModel
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ActivityLogin : AppCompatActivity() {

    val loginRoleKey: String = "loginRoleKey"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContexts.set(this)
        AppUtils.logError()

        val role = getRole()
        LogMe.log("Got Role: $role")
        when (role) {
            Roles.ADMIN -> goToAdminRole()
            Roles.DELIVERY -> goToDeliveryRole()
            Roles.COLLECTOR -> goToCollectorRole()
            Roles.ORDER_COLLECTOR -> goToShowDues()
            else -> logUnIdentifiedDevice()
        }
    }

    private fun logUnIdentifiedDevice() {
        val time = DateUtils.getCurrentTimestamp()
        val id = System.currentTimeMillis().toString()

        val obj = RolesModel(id, time, getPhoneId(), Roles.UNIDENTIFIED.toString())
        PostObject.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.DB_SHEET_ID)
            .tabName(Config.SHEET_TAB_NAME)
            .dataObject(obj as Any)
            .build().execute()
    }

    private fun goToCollectorRole() {
        val switchActivityIntent = Intent(this, CollectorVerifyMoneyCollectionActivity::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun goToDeliveryRole() {
        val loadObj = LoadModel.get()
        if(ActivityDeliveringLoad.isLoadingComplete(loadObj)) {
            goToDeliveringDeliverPage()
        } else {
            val switchActivityIntent = Intent(this, ActivityDeliveringLoad::class.java)
            startActivity(switchActivityIntent)
        }
        finish()
    }

    fun goToDeliveringDeliverPage() {
        val switchActivityIntent = Intent(this, ActivityDeliveringListOrders::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun goToAdminRole() {
        val switchActivityIntent = Intent(this, ActivityAdminDeliveryDashboard::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun goToShowDues() {
        val switchActivityIntent = Intent(this, DueShow::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun getPhoneId(): String {
        return Secure.getString(applicationContext.contentResolver,
            Secure.ANDROID_ID);
    }

    fun getRole(useCache: Boolean = true): Roles? {
        val cacheResults = CentralCache.get<Roles>(AppContexts.get(), loginRoleKey, useCache)

        return if (cacheResults != null) {
            cacheResults
        } else {
            val resultFromServer = getRoleFromServer()

            CentralCache.put(loginRoleKey, resultFromServer)
            resultFromServer
        }
    }

    private fun getRoleFromServer(): Roles? {
        // val waitDialog = ProgressDialog.show(AppContexts.get(), "Please Wait", "লোড হচ্ছে", true)
        val result: GetResponse = Get.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.DB_SHEET_ID)
            .tabName(Config.SHEET_TAB_NAME)
            .build().execute()

        val deviceList = result.parseToObject<RolesModel>(result.getRawResponse(), object: TypeToken<ArrayList<RolesModel>?>() {}.type)
        deviceList.sortBy { it.id }
        deviceList.reverse()

        deviceList.forEach {
            LogMe.log(it.toString())
        }
        deviceList.forEach {
            if(getPhoneId() == it.device_id) {
                return Roles.valueOf(it.role)
            }
        }

        // waitDialog!!.dismiss()
        return null
    }
}