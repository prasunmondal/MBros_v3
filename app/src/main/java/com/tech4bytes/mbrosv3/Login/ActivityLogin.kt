package com.tech4bytes.mbrosv3.Login

//import com.tech4bytes.mbrosv3.BuildConfig
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import com.prasunmondal.dev.libs.caching.CentralCacheObj
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher.DataFetchActivity
import com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher.DataFetchingInfo
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.AppUsers.AppUsersModel
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.UserRoleUtils
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.BuildConfig
import com.tech4bytes.mbrosv3.CollectorVerifyMoneyCollectionActivity
import com.tech4bytes.mbrosv3.Customer.DueShow
import com.tech4bytes.mbrosv3.CustomerAddTransactionActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.adminDashboard.ActivityAdminDeliveryDashboard
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders.ActivityDeliveringListOrders
//import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.ActivityGetCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit.CustomerMoneyDepositUI
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.CustomerTransactions
import com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders.SMSOrdering
import com.tech4bytes.mbrosv3.DeviceIDUtil
import com.tech4bytes.mbrosv3.MoneyCounter.MoneyCounter
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotDelivery
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotLoad
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OneShotSMS
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.io.File


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ActivityLogin : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        setContentView(R.layout.activity_fullscreen)
        supportActionBar!!.hide()
        AppContexts.set(this)
        AppUtils.logError(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        updateAppVerOnUI()
        getDeviceDetails()

        askPermissions(listOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_SMS
        ))

        updateWelcomeDetails()
        Thread {
            val roles = RolesUtils.getAppUser()
            LogMe.log("Got Role: $roles")
            val container = findViewById<LinearLayout>(R.id.activity_login_roles_container)
            if (UserRoleUtils.getUserRoles().isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this, "Registering Device: ${getPhoneId()}", Toast.LENGTH_LONG)
                        .show()

                    // Send SMS when a new device registration is requested.
                    var deviceDetails = Build.BRAND
                    try {
                        SMSUtils.sendSMS(this, "New Registration Requested.\n\nDevice ID: " + getPhoneId() + "\nModel: " + getDeviceDetails(), AppConstants.get(AppConstants.SMS_NUMBER_ON_DEVICE_REG_REQUEST))
                    } catch (e: Exception) {
                        LogMe.log(e, "Failed to Communicate Registration Request.")
                        Toast.makeText(this, "Failed to Communicate Registration Request.", Toast.LENGTH_LONG).show()
                    }
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
                            val entry =
                                layoutInflater.inflate(R.layout.fragment_activity_login_roles, null)

                            val displayText =
                                if (ActivityAuthEnums.getString(role) == null) role.name else ActivityAuthEnums.getString(
                                    role
                                )
                            entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role).text =
                                displayText

                            entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role)
                                .setOnClickListener {
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

    private fun getDeviceDetails(): String {
        val deviceModel = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val androidVersion = Build.VERSION.RELEASE

        return "$manufacturer / $deviceModel / $androidVersion"
    }

    private fun getPendingPermissions(permissionsList: List<String>): MutableList<String> {
        var pendingPermissions = mutableListOf<String>()
        permissionsList.forEach {
            if(checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                pendingPermissions.add(it)
            }
        }
        return pendingPermissions
    }
    private fun askPermissions(permissionsList: List<String>) {
        if(getPendingPermissions(permissionsList).isNotEmpty())
            getReadContactsPermission(permissionsList)
    }

    private fun getReadContactsPermission(permissionsList: List<String>) {
        val PERMISSION_REQUEST_CODE = 101
        Log.d("permission", "permission denied to SEND_SMS - requesting it")
        val permissions = permissionsList.toTypedArray()
        requestPermissions(permissions, PERMISSION_REQUEST_CODE)
    }

    private fun getSMSPermission() {
        val PERMISSION_REQUEST_CODE = 123
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            Log.d("permission", "permission denied to SEND_SMS - requesting it")
            val permissions = arrayOf(android.Manifest.permission.SEND_SMS)
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }
    private fun updateWelcomeDetails() {
        findViewById<TextView>(R.id.welcome_date).text = DateUtils.getCurrentDate("dd")
        findViewById<TextView>(R.id.welcome_month).text = DateUtils.getCurrentDate("MMMM")
        findViewById<TextView>(R.id.welcome_year).text = DateUtils.getCurrentDate("yyyy")
    }

    private fun updateAppVerOnUI() {
        findViewById<TextView>(R.id.app_ver_label).text =
            "App Version: ${BuildConfig.lastGitCommitHash} (${BuildConfig.lastGitCommitDate})"
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
            ActivityAuthEnums.CUSTOMER_TRANSACTIONS -> ::goToCustomerTransactions
            ActivityAuthEnums.WEB_PORTAL -> ::goToWebPortal
            ActivityAuthEnums.MONEY_DEPOSITS -> ::goToMoneyDeposits
            ActivityAuthEnums.COMMUNICATION_CENTER -> ::goToSendSMSPage
            ActivityAuthEnums.ADD_TRANSACTION -> ::goToAddCustomerTransaction
            else -> null
        }
    }

    private fun goToMoneyDeposits() {
        goToDataFetchActivity(ActivityAuthEnums.MONEY_DEPOSITS, CustomerMoneyDepositUI::class.java)
    }

    private fun goToWebPortal() {
        val url = AppConstants.get(AppConstants.WEB_PORTAL_URL)
        val urlString = url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            this.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null)
            this.startActivity(intent)
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

        val obj = AppUsersModel(
            id,
            time,
            getPhoneId(),
            ActivityAuthEnums.UNIDENTIFIED.toString(),
            AuthorizationEnums.NONE.toString()
        )
        RolesUtils.insert(obj).execute()
        CentralCacheObj.centralCache.invalidateFullCache(AppContexts.get())
    }

    private fun goToCollectorRole() {
        goToDataFetchActivity(
            ActivityAuthEnums.COLLECTOR,
            CollectorVerifyMoneyCollectionActivity::class.java
        )
    }

    private fun goToGetOrdersPage() {
//        goToDataFetchActivity(
//            ActivityAuthEnums.ORDER_COLLECTOR,
//            ActivityGetCustomerOrders::class.java
//        )
    }

    private fun goToDeliveringDeliverPage() {
        goToDataFetchActivity(ActivityAuthEnums.DELIVERY, ActivityDeliveringListOrders::class.java)
    }

    private fun goToAdminRole() {
        goToDataFetchActivity(ActivityAuthEnums.ADMIN, ActivityAdminDeliveryDashboard::class.java)
    }

    fun goToSendSMSPage() {
        goToDataFetchActivity(ActivityAuthEnums.COMMUNICATION_CENTER, OneShotSMS::class.java)
    }

    fun goToAddCustomerTransaction() {
        goToDataFetchActivity(ActivityAuthEnums.ADD_TRANSACTION, CustomerAddTransactionActivity::class.java)
    }

    private fun goToCustomerTransactions() {
        goToDataFetchActivity(
            ActivityAuthEnums.CUSTOMER_TRANSACTIONS,
            CustomerTransactions::class.java
        )
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
        runOnUiThread {
            Toast.makeText(AppContexts.get(), "Device Registration Pending. Connect Admin", Toast.LENGTH_LONG).show()
        }

        try {
            SMSUtils.sendSMS(this, "MBros\nFollow Up: New Registration Requested.\n\nDevice ID: " + getPhoneId() + " - " + getDeviceDetails(), AppConstants.get(AppConstants.SMS_NUMBER_ON_DEVICE_REG_REQUEST))
        } catch (e: Exception) {
            LogMe.log(e, "Failed to Communicate Registration Request.")
            Toast.makeText(this, "Failed to Communicate Registration Request.", Toast.LENGTH_LONG).show()
        }
        CentralCacheObj.centralCache.invalidateFullCache(AppContexts.get())
    }

    @SuppressLint("HardwareIds")
    private fun getPhoneId(): String {
        return DeviceIDUtil(AppContexts.get()).getUniqueID()
    }

    fun loginPageOnClickRefresh(view: View) {
        val refreshBtn = findViewById<TextView>(R.id.menu_refreshBtn)
        refreshBtn.isEnabled = false
        refreshBtn.alpha = .5f
        refreshBtn.isClickable = false
        refreshBtn.text = "Refreshing... .. ."
        AppUtils.invalidateAllDataAndRestartApp()
    }

    fun onClickUpdate(view: View) {
        downloadApk(this)
    }

    @SuppressLint("Range")
    fun downloadApk(context: Context) {
        val request = DownloadManager.Request(Uri.parse("https://github.com/prasunmondal/MBros_v3/releases/download/1.0.1/MBros-a0d0524f-2024.10.27-debug.apk"))
        request.setTitle("App Update")
        request.setDescription("Downloading new version")
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "app-update.apk")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var downloadId: Long = 30
        try {
            downloadId = downloadManager.enqueue(request)
        } catch (e: Exception)
        {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(columnIndex)
                if (status == DownloadManager.STATUS_FAILED) {
                    val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    Log.e("Download Error", "Failed with reason: $reason")
                }
            }
        }

        try {
            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    try {
                        if (intent != null && intent.getLongExtra(
                                DownloadManager.EXTRA_DOWNLOAD_ID,
                                -1
                            ) == downloadId
                        ) {
                            val uri = downloadManager.getUriForDownloadedFile(downloadId)
                            if (uri != null) {
                                installApk(context!!, uri)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Download failed or URI is null",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            context?.unregisterReceiver(this)
                        }
                    } catch (e: Exception) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_FAILED) {
                                val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                                Log.e("Download Error", "Failed with reason: $reason")
                            }
                        }
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Error during download: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        } catch (e: Exception) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(columnIndex)
                if (status == DownloadManager.STATUS_FAILED) {
                    val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    Log.e("Download Error", "Failed with reason: $reason")
                }
            }
        }
    }

    fun installApk(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val apkFileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    File(apkUri.path!!)
                )
                data = apkFileUri
            } else {
                data = apkUri
            }
        }
        context.startActivity(intent)
    }
}
