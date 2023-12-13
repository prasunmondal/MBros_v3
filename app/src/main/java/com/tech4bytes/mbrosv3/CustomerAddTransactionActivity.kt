package com.tech4bytes.mbrosv3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Payments.Staged.StagedPay
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class CustomerAddTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_add_transaction)

        setUI()
    }

    fun setUI(useCache: Boolean = true) {
        Thread {
            setUICheckFinalization(false)
        }.start()

        Thread {
            setCustomerNameDropdown(useCache)
        }.start()
    }

    private fun setCustomerNameDropdown(useCache: Boolean = true) {
        val customerNamesSpinner = findViewById<Spinner>(R.id.addTransaction_name)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, CustomerData.getAllCustomerNames(useCache)
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
        if(name.trim().isEmpty()) {
            runOnUiThread {
                uiIndicator.text = "Select Customer"
            }
        }

        val currentBalance = CustomerDueData.getBalance(name)

        runOnUiThread {
            uiIndicator.text = NumberUtils.getIntOrZero(currentBalance.toString()).toString()
        }
    }

    private fun setUICheckFinalization(useCache: Boolean) {
        val isFinalized = DaySummary.isDayFinalized(useCache)
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
    
    fun onClickSubmitBtn(view: View) {
        val name = findViewById<Spinner>(R.id.addTransaction_name).selectedItem.toString()
        val prevAmount = CustomerDueData.getLastFinalizedDue(name, false)
        val paidAmount = findViewById<EditText>(R.id.addTransaction_amount).text.toString()
        val notes = findViewById<EditText>(R.id.addTransaction_note).text.toString()

        val selectedTxnTypeElement = findViewById<RadioGroup>(R.id.addTransaction_txn_type).checkedRadioButtonId
        val txnType = findViewById<RadioButton>(selectedTxnTypeElement).text.toString().uppercase()

        val selectedTxnModeElement = findViewById<RadioGroup>(R.id.addTransaction_txn_mode).checkedRadioButtonId
        val txnMode = findViewById<RadioButton>(selectedTxnModeElement).text.toString().uppercase()

        StagedPay.transact(name, prevAmount, txnType, paidAmount, txnMode, notes)
    }
}