package com.tech4bytes.mbrosv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.tech4bytes.mbrosv3.Sms.SmsReader
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

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

        smsFiltered.forEach {
            val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_fragments, null)
            entry.findViewById<TextView>(R.id.smsorder_body).text = "${it.number}: ${it.body}\n - ${it.datetime}"
            container.addView(entry)
        }
    }
}