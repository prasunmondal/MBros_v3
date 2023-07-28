package com.tech4bytes.mbrosv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Sms.SmsReader
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class SMSOrdering : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smsordering)
        AppContexts.set(this)

        showSMS()
    }

    fun showSMS() {
        val allSMS = SmsReader.getAllSms(this)
        val smsFiltered = SmsReader.getSMSStartingWith(SmsReader.getSMSFromNumber(allSMS, "9734075801"), "")
        val container = findViewById<LinearLayout>(R.id.smsorders_sms_view_container)

        smsFiltered.forEach { sms ->
            val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_fragments, null)
            entry.findViewById<TextView>(R.id.smsorder_listEntry_name).text = "${sms.number}: ${sms.body}\n - ${sms.datetime}"
            container.addView(entry)
            entry.setOnClickListener {
                processSMS(sms.body)
            }
        }
    }

    private fun processSMS(valueStr: String) {
        val valueArray = valueStr.split("+")
        var namesArray = SingleAttributedData.getRecords().smsOrderSequence.split(",")
        val minSize = Math.min(valueArray.size, namesArray.size)
        val orderListContainer = findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        orderListContainer.removeAllViews()

        var totalKg = 0
        for (j in 0 until minSize) {
            if(NumberUtils.getIntOrZero(valueArray[j].trim()) != 0) {
                val balance = CustomerDueData.getBalance(namesArray[j].trim())
                val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
                totalKg += NumberUtils.getIntOrZero(valueArray[j].trim())
                entry.findViewById<TextView>(R.id.smsorder_listEntry_kg).text = valueArray[j].trim()
                entry.findViewById<TextView>(R.id.smsorder_listEntry_name).text = namesArray[j]
                entry.findViewById<TextView>(R.id.smsorder_listEntry_amount).text = "$balance"
                orderListContainer.addView(entry)
            }
        }

        // Show Total
        val totalEntry = layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
        totalEntry.findViewById<TextView>(R.id.smsorder_listEntry_kg).text = "${totalKg}"
        totalEntry.findViewById<TextView>(R.id.smsorder_listEntry_name).text = "TOTAL"
        totalEntry.findViewById<TextView>(R.id.smsorder_listEntry_amount).text = ""
        orderListContainer.addView(totalEntry)

    }
}