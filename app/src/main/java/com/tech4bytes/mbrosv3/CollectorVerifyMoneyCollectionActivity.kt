package com.tech4bytes.mbrosv3

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliveringUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OtherExpensesActivity
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import java.io.File


class CollectorVerifyMoneyCollectionActivity : AppCompatActivity() {

    var doneCounter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_verify_money_collection)
        AppContexts.set(this)
        AppUtils.logError(this)

//        setContentView(R.layout.activity_fullscreen)
        supportActionBar!!.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        showDeliveryData()
    }


//    fun downloadDailySheet() {
//        DownloadFiles()
//        val manager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        val uri = Uri.parse("https://docs.google.com/spreadsheets/d/e/2PACX-1vQSO3BWQ7b0JmySpKVSULco9FcxrDi3UX9uSIECvOdUCSUI8AyeCDjSnmwWeA-l6oHBkUNhDjTU7Rgd/pub?gid=1385397548&single=true&output=pdf")
//        val request = DownloadManager.Request(uri)
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//        request.setDestinationUri(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toUri())
//
//        request.setTitle(filename)
//        request.
//        val reference: Long = manager.enqueue(request)
////        sharePDF(File(manager.getUriForDownloadedFile(reference).toString()))
//    }

    private fun sharePDF(file: File) {
        //  val file = File(pdfFilePath)
        val URI = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, URI)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    fun sendDailySheet(filename: String) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
        val uri = FileProvider.getUriForFile(this, this.packageName + ".provider", outputFile)

//        val uri: Uri = Uri.fromFile(outputFile)

        LogMe.log(outputFile.toUri().toString())
//        val photoURI = FileProvider.getUriForFile(applicationContext, applicationContext.applicationContext.packageName + ".provider", outputFile)

        val share = Intent()
        share.action = Intent.ACTION_SEND
        share.type = "application/pdf"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.setPackage("com.whatsapp")

        startActivity(Intent.createChooser(share, ""));
    }

    class VerifyElements {
        var kgPc: Boolean = false
        var paidAmount: Boolean = false
    }

    var map: MutableMap<String, VerifyElements> = mutableMapOf()

    private fun showDeliveryData() {
        var deliveredData = DeliveringUtils.fetchAll().execute()
        var bundlesCount = 0

        val listContainer = findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_container)
        deliveredData = ListUtils.sortListByAttribute(deliveredData, DeliverToCustomerDataModel::id)
        deliveredData.forEach { deliveryEntry ->
            map[deliveryEntry.name] = VerifyElements()
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_collector_verify_money_collection_entries, null)
            val amountPaidCashField = entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_paid_amount_cash)
            val amountPaidOnlineField = entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_paid_amount_online)
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_name).text = deliveryEntry.name
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_order_pc).text = deliveryEntry.deliveredKg
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_order_kg).text = deliveryEntry.deliveredPc
            amountPaidCashField.text = deliveryEntry.paidCash
            if(NumberUtils.getIntOrZero(deliveryEntry.paidOnline) != 0) {
                amountPaidOnlineField.text = "\uD83C\uDF10 Rs ${deliveryEntry.paidOnline}"
            }
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_total_due_amount).text = deliveryEntry.khataBalance
            initiallizeColors(entry, NumberUtils.getIntOrZero(amountPaidCashField.text.toString()), map[deliveryEntry.name]!!.kgPc, map[deliveryEntry.name]!!.paidAmount)
            doneCounter = 0

            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container).setOnClickListener {
                map[deliveryEntry.name]!!.kgPc = !map[deliveryEntry.name]!!.kgPc
                map[deliveryEntry.name]!!.paidAmount = !map[deliveryEntry.name]!!.paidAmount
                updateColors(entry, NumberUtils.getIntOrZero(amountPaidCashField.text.toString()), map[deliveryEntry.name]!!.kgPc, map[deliveryEntry.name]!!.paidAmount)
            }

            listContainer.addView(entry)

            if (NumberUtils.getIntOrZero(deliveryEntry.paidCash) > 0) {
                bundlesCount++
            }
        }

        updateSummary(bundlesCount)
    }

    private fun updateSummary(bundlesCount: Int) {
        val bundles = findViewById<TextView>(R.id.vmc_number_of_money_bundles)
        bundles.text = bundlesCount.toString()
    }

    private fun initiallizeColors(entry: View, paidAmount: Int, isKgPcVerified: Boolean, isPaidAmountVerified: Boolean) {
        if(paidAmount == 0) {
            return
        }
        if (isKgPcVerified && isPaidAmountVerified) {
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
        } else {
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_not_valid))
        }
    }

    private fun updateColors(entry: View, paidAmount: Int, isKgPcVerified: Boolean, isPaidAmountVerified: Boolean) {
        if(paidAmount == 0) {
            return
        }
        if (isKgPcVerified && isPaidAmountVerified) {
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_seq_no).text = "${++doneCounter}"
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
        } else {
            entry.findViewById<TextView>(R.id.activity_collector_verify_money_collection_fragment_customer_seq_no).text = ""
            --doneCounter
            entry.findViewById<LinearLayout>(R.id.activity_collector_verify_money_collection_fragment_container)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_not_valid))
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }

//    fun onClickDownloadDailyFile(view: View) {
//        Toast.makeText(this, "Downloading Daily File", Toast.LENGTH_SHORT).show()
//        val filename = "MBros - ${DateUtils.getDateInFormat(Date(), "yyyy.MM.dd")}"
//        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI().toPath().toString() + "/" + filename + ".pdf"
//
//        Thread {
//            downloadDailySheet(filePath)
//            runOnUiThread {
//                Toast.makeText(this, "Download Complete", Toast.LENGTH_SHORT).show()
//            }
//            Whatsapp.sendFileToWhatsapp(this, filePath, "919679004046", "")
//        }.start()
//    }

    fun onClickPopulateOtherExpenses(view: View) {
        val switchActivityIntent = Intent(this, OtherExpensesActivity::class.java)
        startActivity(switchActivityIntent)
    }
}