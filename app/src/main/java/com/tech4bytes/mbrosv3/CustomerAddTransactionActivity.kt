package com.tech4bytes.mbrosv3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.OneShot.Delivery.OneShotDelivery
import com.tech4bytes.mbrosv3.Payments.PaymentsType
import com.tech4bytes.mbrosv3.Payments.Staged.StagedPaymentsModel
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OneShotSMS
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.SMS
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.SMSParser
import com.tech4bytes.mbrosv3.Sms.SMSProcessors.SMSProcessor.SMSProcessor
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.Locale

class CustomerAddTransactionActivity : AppCompatActivity() {
    private var smsList: MutableList<SMS> = mutableListOf()
    private lateinit var nameElement: Spinner
    private lateinit var prevAmount: EditText
    private lateinit var paidAmountElement: EditText
    private lateinit var notesElement: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_add_transaction)
        AppContexts.set(this)
        nameElement = findViewById(R.id.addTransaction_name)
        paidAmountElement = findViewById(R.id.addTransaction_amount)
        notesElement = findViewById(R.id.addTransaction_note)

        setUI()
    }

    fun setUI(useCache: Boolean = true) {
        Thread {
            setUICheckFinalization(false)
        }.start()

        Thread {
            setCustomerNameDropdown(useCache)
        }.start()

        paidAmountElement.addTextChangedListener {
            Thread {
                generateMessage()
            }.start()
        }
    }

    private fun setCustomerNameDropdown(useCache: Boolean = true) {
        val customerNamesSpinner = findViewById<Spinner>(R.id.addTransaction_name)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, CustomerDataUtils.getAllCustomerNames(useCache)
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        customerNamesSpinner.setAdapter(adapter)

        customerNamesSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long,
            ) {
                Thread {
                    setCurrentBalance()
                }.start()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Thread {
                    setCurrentBalance()
                }.start()
            }
        })
    }

    private fun setCurrentBalance() {
        val uiIndicator = findViewById<TextView>(R.id.addTransaction_currentBalance)
        val name = findViewById<Spinner>(R.id.addTransaction_name).selectedItem.toString()
        if (name.trim().isEmpty()) {
            runOnUiThread {
                uiIndicator.text = "Select Customer"
            }
        }

        val currentBalance = CustomerDueData.getBalance(name)

        runOnUiThread {
            uiIndicator.text = NumberUtils.getIntOrZero(currentBalance.toString()).toString()
        }
    }

    private fun setUICheckFinalization(useCache: Boolean = true) {
        val isFinalized = DaySummaryUtils.isDayFinalized(useCache)
        val uiIndicator = findViewById<TextView>(R.id.addTransaction_finalizedStatus)

        runOnUiThread {
            if (isFinalized) {
                uiIndicator.text = "Data Finalized"
                uiIndicator.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_valid))
            } else {
                uiIndicator.text = "Data Not Finalized. Finalize it first, or rerun OSD"
                uiIndicator.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_not_valid))
            }
        }
    }
    private fun generateMessage() {
        val smsViewContainer = findViewById<LinearLayout>(R.id.ACT_messageViewer)
        runOnUiThread {
            OneShotSMS.showMessages(smsViewContainer, mutableListOf())
        }
        smsList = SMSProcessor.getSMSList("PaymentIntimations", getObjFromUI())
        runOnUiThread {
            OneShotSMS.showMessages(smsViewContainer, smsList)
        }
    }

    fun onClickSubmitBtn(view: View) {
        Toast.makeText(this, "Not Yet Supported!", Toast.LENGTH_LONG).show()
//        StagedPay.transact(getObjFromUI())
    }

    private fun getObjFromUI(): StagedPaymentsModel {
        val selectedTxnTypeElement = findViewById<RadioGroup>(R.id.addTransaction_txn_type).checkedRadioButtonId
        val txnType = findViewById<RadioButton>(selectedTxnTypeElement).text.toString().uppercase()

        val selectedTxnModeElement = findViewById<RadioGroup>(R.id.addTransaction_txn_mode).checkedRadioButtonId
        val txnMode = findViewById<RadioButton>(selectedTxnModeElement).text.toString().uppercase()

        val stagedObj = StagedPaymentsModel(
            id = System.currentTimeMillis().toString(),
            datetime = DateUtils.getCurrentTimestamp(),
            name = nameElement.selectedItem.toString(),
            balanceBeforePayment = CustomerDueData.getBalance(nameElement.selectedItem.toString()).toString(),
            transactionType = PaymentsType.valueOf(txnType.uppercase(Locale.ROOT)),
            paidAmount = paidAmountElement.text.toString(),
            paymentMode = txnMode,
            notes = notesElement.text.toString(),
            newBalance = "0"
        )

        var paidAmountInt = NumberUtils.getIntOrZero(stagedObj.paidAmount)
        if (stagedObj.transactionType == PaymentsType.DEBIT) {
            paidAmountInt *= -1
        }
        stagedObj.newBalance = (NumberUtils.getIntOrZero(stagedObj.balanceBeforePayment) - paidAmountInt).toString()

        return stagedObj
    }

    fun onClickSendMessageBtn(view: View) {

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

    fun onClickSendReport(view: View) {
        val switchActivityIntent = Intent(this, OneShotSMS::class.java)
        val bundle = Bundle()
        bundle.putString("communication_selector_type", "Shareholder")
        bundle.putBoolean("useCache", false)
        switchActivityIntent.putExtras(bundle)
        startActivity(switchActivityIntent)
    }

    fun onClickGoToDelivery(view: View) {
        val switchActivityIntent = Intent(this, OneShotDelivery::class.java)
        startActivity(switchActivityIntent)
    }
}