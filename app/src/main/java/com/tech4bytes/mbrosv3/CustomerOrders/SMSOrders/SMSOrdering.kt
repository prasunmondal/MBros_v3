package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.SmsReader
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.math.RoundingMode
import java.text.DecimalFormat


class SMSOrdering : AppCompatActivity() {

    var orders = mutableListOf<SMSOrderModel>()
    var smsToProcess: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smsordering)
        AppContexts.set(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setUpListeners()
        showSMS()
    }

    private fun setUpListeners() {
        findViewById<EditText>(R.id.smsorder_avg_wt1).doOnTextChanged { text, start, before, count ->
            processSMS()
            showEntries()
            showTotal()
        }
    }

    fun showSMS() {
        val allSMS = SmsReader.getAllSms(this)
        val smsFiltered = SmsReader.getSMSStartingWith(SmsReader.getSMSFromNumber(allSMS, AppConstants.get(AppConstants.SMS_ORDER_GET_ORDER_PH_NUMBER)), "")
        val container = findViewById<LinearLayout>(R.id.smsorders_sms_view_container)

        smsToProcess = "100+50+0+0+40+40+0+30+30+20+40+20+10+120"
        processSMS()
        showEntries()
        showTotal()

        smsFiltered.forEach { sms ->
            val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_fragments, null)
            entry.findViewById<TextView>(R.id.smsorder_listEntry_name).text = "${sms.number}: ${sms.body}\n - ${sms.datetime}"
            container.addView(entry)
            entry.setOnClickListener {
                smsToProcess = sms.body
                processSMS()
                showEntries()
                showTotal()
            }
        }
    }

    private fun processSMS() {
        val valueStr = smsToProcess
        val valueArray = valueStr.split("+")
        val namesArray = AppConstants.get(AppConstants.SMS_ORDER_SEQUENCE).split(",")
        val minSize = Math.min(valueArray.size, namesArray.size)

        var totalKg = 0
        var totalPc = 0
        orders = mutableListOf()
        for (j in 0 until minSize) {
            if (NumberUtils.getIntOrZero(valueArray[j].trim()) != 0) {
                totalKg += NumberUtils.getIntOrZero(valueArray[j].trim())
                var avgWt1 = getAvgWt1()
                if (avgWt1 == 0.0) {
                    avgWt1 = 1.0
                }
                val calculatedPc = NumberUtils.getIntOrZero(valueArray[j].trim()) / avgWt1
                var df = DecimalFormat("#.#")
                val calculatedPcDouble = df.format(calculatedPc).toDouble()

                df = DecimalFormat("#")
                df.roundingMode = RoundingMode.CEILING
                val finalizedPc1Double = df.format(calculatedPcDouble).toDouble()
                val finalizedPc1 = finalizedPc1Double.toInt()
                totalPc += finalizedPc1

                orders.add(SMSOrderModel(System.currentTimeMillis().toString(), namesArray[j].trim(), valueArray[j].trim().toInt(), calculatedPcDouble, finalizedPc1, CustomerDueData.getBalance(namesArray[j].trim())))
            }
        }
    }

    private fun showEntries() {
        val orderListContainer = findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        orderListContainer.removeAllViews()
        for (j in 0 until orders.size) {
            val balance = orders[j].prevDue
            val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
            entry.findViewById<TextView>(R.id.smsorder_listEntry_calculated_pc).text = orders[j].calculatedPc.toString()

            val finalizedPcView = entry.findViewById<EditText>(R.id.smsorder_listEntry_pc)
            finalizedPcView.hint = orders[j].orderedPc.toString()
            finalizedPcView.doOnTextChanged { text, start, before, count ->
                orders[j].orderedPc = NumberUtils.getIntOrZero(finalizedPcView.text.toString())
                if(finalizedPcView.text.toString() == "") {
                    orders[j].orderedPc = NumberUtils.getIntOrZero(finalizedPcView.hint.toString())
                }
                updateTotal()
            }

            entry.findViewById<TextView>(R.id.smsorder_listEntry_kg).text = orders[j].orderedKg.toString()
            entry.findViewById<TextView>(R.id.smsorder_listEntry_name).text = orders[j].name
            entry.findViewById<TextView>(R.id.smsorder_listEntry_amount).text = "$balance"
            orderListContainer.addView(entry)
        }
    }

    var totalEntryView: View? = null
    fun showTotal() {
        val orderListContainer = findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        totalEntryView = layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
        updateTotal()
        orderListContainer.addView(totalEntryView)
    }

    fun updateTotal() {
        var totalKg = 0
        var totalPc = 0
        for (j in 0 until orders.size) {
            totalPc += orders[j].orderedPc
            totalKg += orders[j].orderedKg
        }

        val totalPcsField = totalEntryView?.findViewById<EditText>(R.id.smsorder_listEntry_pc)
        totalPcsField?.setText(totalPc.toString())
        totalPcsField?.setTextColor(ContextCompat.getColor(this, androidx.appcompat.R.color.material_blue_grey_800))
        totalPcsField?.setTypeface(null, Typeface.BOLD)

        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_kg)?.text = "$totalKg"
        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_name)?.text = "TOTAL"
        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_amount)?.text = ""
    }

    fun getAvgWt1(): Double {
        return NumberUtils.getDoubleOrZero(findViewById<EditText>(R.id.smsorder_avg_wt1).text.toString())
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }

    fun onClickSaveSMSOrdersBtn(view: View) {
        val saveBtn = view as Button
        Thread {
            runOnUiThread {
                saveBtn.isEnabled = false
                saveBtn.alpha = .5f
                saveBtn.isClickable = false
                saveBtn.text = "Deleting previous data"
            }

            SMSOrderModel.deleteAllDataInServer()
            var count = 1
            runOnUiThread {
                saveBtn.text = "Saving ($count/${orders.size})"
                count++
            }
            orders.forEach {
                SMSOrderModel.save(it)
            }

            runOnUiThread {
                saveBtn.isEnabled = true
                saveBtn.alpha = 1.0f
                saveBtn.isClickable = true
                saveBtn.text = "Save"
            }
        }.start()
    }
}