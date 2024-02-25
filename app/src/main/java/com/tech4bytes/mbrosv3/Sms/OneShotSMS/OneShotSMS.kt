package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.ContactsUtils.Contacts
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils


class OneShotSMS : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var smsList: MutableList<SMS>
    private lateinit var communication_type_selector: AutoCompleteTextView
    private lateinit var refreshBtn: AppCompatImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_sms)
        supportActionBar!!.hide()
        AppContexts.set(this)

        container = findViewById(R.id.osms_container)
        communication_type_selector = findViewById(R.id.comcentre_communication_type)
        refreshBtn = findViewById(R.id.msg_cntr_refresh_btn)

        Thread {
            initializeListeners()
            processAndShowMessages()
        }.start()
    }

    private fun initializeListeners() {
        refreshBtn.setOnClickListener {
            processAndShowMessages(false)
        }
    }
    fun processAndShowMessages(useCache: Boolean = true) {
        OSMS.get(useCache)
        val selectedType = initializeCommunicationSelector()
        Contacts.getContactList(this, false)
        selectCommunication(selectedType)
    }

    private fun initializeCommunicationSelector(): String {
        val communicationSelectorOptions = getCommunicationSelectorOptions()

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.template_dropdown_entry, communicationSelectorOptions)
        runOnUiThread {
            communication_type_selector.setAdapter(adapter)
            communication_type_selector.setText(communicationSelectorOptions[0], false)
            communication_type_selector.threshold = 0
            communication_type_selector.setOnTouchListener { _, _ ->
                communication_type_selector.showDropDown()
                communication_type_selector.requestFocus()
                false
            }
        }
        communication_type_selector.setOnItemClickListener { adapterView, view, i, l ->
            selectCommunication(communication_type_selector.text.toString())
        }
        return communicationSelectorOptions[0]
    }

    private fun selectCommunication(selectedCommunications: String) {
//        val finalSelectedCommunication = selectedCommunications ?: communication_type_selector.text.toString()
        smsList = getSMSList(selectedCommunications)
        runOnUiThread {
            showMessages()
        }
    }

    private fun getCommunicationSelectorOptions(): List<String> {
        var result = mutableListOf<String>()
        val smsRawList = OSMS.get()
        smsRawList.forEach { sms ->
            sms.commReceiverCategory.split(",").forEach { commType ->
                result.add(commType)
            }
        }
        return ListUtils.sortListByFrequency(result)
    }

    private fun showMessages() {
        container.removeAllViews()
        var i = 0
        smsList.forEach { msg ->
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val uiEntry = layoutInflater.inflate(R.layout.activity_one_shot_sms_entry_fragment, null)
            val contentUI = uiEntry.findViewById<CheckBox>(R.id.osms_entry_sms_content)

            val text = "${Contacts.getNameByNumber(msg.number, "")} - ${msg.number} (${msg.medium})\n\n${msg.text}"
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

    private fun getIndividualCommType(oSmsModel: OSMSModel): List<String> {
        return oSmsModel.commReceiverCategory.split(",")
    }
    fun onClickSendSMS(view: View) {
        smsList.forEach {
            if (it.isEnabled) {
                OSMSProcessor.sendViaDesiredMedium(it.medium, it.number, it.text)
            }
        }
    }

    private fun getSMSList(selectedCommunications: String): MutableList<SMS> {
        LogMe.log("Getting messages for type: $selectedCommunications")
        val smsList: MutableList<SMS> = mutableListOf()
        OSMS.get().forEach {
            LogMe.log("Looking for: $selectedCommunications");
            getIndividualCommType(it).forEach {r ->
                LogMe.log("..$r..")
            }

            if (it.isEnabled.toBoolean() && getIndividualCommType(it).contains(selectedCommunications)) {
                val smsResult: List<SMS>? = when (it.communicationType) {
                    "DELIVERY_SMS" -> OSMSProcessor.sendDeliverySMS(it)
                    "DAY_SUMMARY" -> OSMSProcessor.sendDaySummary(it)
                    "LOAD_DETAILS" -> OSMSProcessor.sendLoadDetails(it)
                    "BULK_DELIVERY_SMS" -> OSMSProcessor.sendBulkDeliverySMS(it)
                    else -> null
                }
                if (smsResult != null)
                    smsList.addAll(smsResult)
            }
        }
        smsList.forEach { sms ->
            LogMe.log(sms.toString())
        }
        return smsList
    }
}