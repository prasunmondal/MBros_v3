package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class OneShotSMS : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var smsList: MutableList<SMS>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_sms)
        supportActionBar!!.hide()
        AppContexts.set(this)

        container = findViewById(R.id.osms_container)
        Thread {
            smsList = getSMSList()
            runOnUiThread {
                showMessages()
            }
        }.start()
    }

    private fun showMessages() {
        container.removeAllViews()
        var i = 0
        smsList.forEach { msg ->
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val uiEntry = layoutInflater.inflate(R.layout.activity_one_shot_sms_entry_fragment, null)
            val contentUI = uiEntry.findViewById<CheckBox>(R.id.osms_entry_sms_content)

            val text = "${msg.number} (${msg.medium})\n\n${msg.text}"
            contentUI.text = text
            contentUI.isChecked = msg.isEnabled

            contentUI.setOnClickListener {
                msg.isEnabled = !msg.isEnabled
                contentUI.isChecked = msg.isEnabled
            }
            container.addView(uiEntry)
            i++
        }
    }

    fun onClickSendSMS(view: View) {
        smsList.forEach {
            if (it.isEnabled) {
                OSMSProcessor.sendViaDesiredMedium(it.medium, it.number, it.text)
            }
        }
    }

    private fun getSMSList(): MutableList<SMS> {
        val smsList: MutableList<SMS> = mutableListOf()
        OSMS.get().forEach {
            if (it.isEnabled.toBoolean()) {
                val smsResult: SMS? = when (it.communicationType) {
                    "DELIVERY_SMS" -> OSMSProcessor.sendDeliverySMS(it)
                    "DAY_SUMMARY" -> OSMSProcessor.sendDaySummary(it)
                    "LOAD_DETAILS" -> OSMSProcessor.sendLoadDetails(it)
                    else -> null
                }
                if (smsResult != null)
                    smsList.add(smsResult)
            }
        }
        return smsList
    }
}