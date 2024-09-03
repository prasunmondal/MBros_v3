package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.tech4bytes.mbrosv3.MoneyCounter.MoneyCounter
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.SMSProcessors.SMSProcessor.SMSProcessor
import com.tech4bytes.mbrosv3.Utils.ContactsUtils.Contacts
import com.prasunmondal.dev.libs.contexts.AppContexts
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

        val inputCommunicationSelectorOption = try {intent.extras!!.get("communication_selector_type") as String} catch (e: Exception) {""}
        val inputUseCache = try {intent.extras!!.get("useCache") as Boolean} catch (e: Exception) {true}

        container = findViewById(R.id.osms_container)
        communication_type_selector = findViewById(R.id.comcentre_communication_type)
        refreshBtn = findViewById(R.id.msg_cntr_refresh_btn)

        Thread {
            initializeListeners(inputCommunicationSelectorOption)
            processAndShowMessages(inputCommunicationSelectorOption, inputUseCache)
        }.start()
    }

    private fun initializeListeners(communicationSelectorOption: String) {
        refreshBtn.setOnClickListener {
            processAndShowMessages(communicationSelectorOption, false)
        }
    }
    fun processAndShowMessages(communicationSelectorOption: String, useCache: Boolean = true) {
        OSMS.get(useCache)
        val selectedType = initializeCommunicationSelector(communicationSelectorOption)
        Contacts.getContactList(this, false)
        smsList = selectCommunication(selectedType)
        runOnUiThread {
            showMessages(container, smsList)
        }
    }

    private fun initializeCommunicationSelector(communicationSelectorOption: String): String {
        val communicationSelectorOptions = getCommunicationSelectorOptions()

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.template_dropdown_entry, communicationSelectorOptions)
        runOnUiThread {
            communication_type_selector.setAdapter(adapter)

            if(communicationSelectorOption.isNotBlank() && communicationSelectorOptions.contains(communicationSelectorOption))
                communication_type_selector.setText(communicationSelectorOption, false)
            else
                communication_type_selector.setText(communicationSelectorOptions[0], false)


            communication_type_selector.threshold = 0
            communication_type_selector.setOnTouchListener { _, _ ->
                communication_type_selector.showDropDown()
                communication_type_selector.requestFocus()
                false
            }
        }
        communication_type_selector.setOnItemClickListener { adapterView, view, i, l ->
            smsList = selectCommunication(communication_type_selector.text.toString())
            runOnUiThread {
                showMessages(container, smsList)
            }
        }
        return communicationSelectorOptions[0]
    }

    fun selectCommunication(selectedCommunications: String): MutableList<SMS> {
//        val finalSelectedCommunication = selectedCommunications ?: communication_type_selector.text.toString()
        return SMSProcessor.getSMSList(selectedCommunications)
    }

    private fun getCommunicationSelectorOptions(): List<String> {
        var result = mutableListOf<String>()
        val smsRawList = OSMS.get()
        smsRawList.forEach { sms ->
            if(sms.isAuthorized()) {
            sms.commReceiverCategory.split(",").forEach { commType ->
                result.add(commType)
            }
            }
        }
        return ListUtils.sortListByFrequency(result)
    }

    fun onClickSendSMS(view: View) {
        val numberOfSMSToSend = smsList.stream().filter{ it.isEnabled }.count()

        AlertDialog.Builder(this)
            .setTitle("Send Messages?")
            .setMessage("Sending $numberOfSMSToSend messages? \nR U Sure?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                Toast.makeText(
                    this,
                    "",
                    Toast.LENGTH_SHORT
                ).show()
                smsList.forEach {
                    if (it.isEnabled) {
                        SMSParser.sendViaDesiredMedium(it.medium, it.number, it.text)
                    }
                }
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    fun goToCountMoney(view: View) {
        startActivity(Intent(this, MoneyCounter::class.java))
    }

    companion object {
        fun showMessages(container: LinearLayout, smsList: MutableList<SMS>) {
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
    }
}