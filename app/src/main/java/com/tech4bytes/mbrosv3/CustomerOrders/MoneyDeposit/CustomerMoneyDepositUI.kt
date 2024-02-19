package com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit.CustomerDeposit.CustomerMoneyDeposit
import com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit.CustomerDeposit.CustomerMoneyDepositModel
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import java.util.stream.Collectors

class CustomerMoneyDepositUI : AppCompatActivity() {

    lateinit var beneficiaryView: AutoCompleteTextView
    lateinit var paymentModeView: AutoCompleteTextView
    lateinit var debitAccountListView: AutoCompleteTextView
    lateinit var creditAccountView: AutoCompleteTextView
    lateinit var creditAmountView: EditText
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_deposit)

        initiallizeUIVariables()
        val allTransactionRecords = CustomerMoneyDeposit.get(false)
        populateBeneficiary(allTransactionRecords)
    }

    private fun initiallizeUIVariables() {
        beneficiaryView = findViewById(R.id.md_beneficiary)
        paymentModeView = findViewById(R.id.md_transfer_mode)
        debitAccountListView = findViewById(R.id.md_debit_account)
        creditAccountView = findViewById(R.id.md_credit_account)
        creditAmountView = findViewById(R.id.md_credit_amount)
    }

    private fun populateBeneficiary(allTransactionRecords: List<CustomerMoneyDepositModel>) {
        var beneficiaryList: List<String> = allTransactionRecords.stream()
            .map(CustomerMoneyDepositModel::beneficiary)
            .collect(Collectors.toList())

        if(beneficiaryList.isEmpty())
            return

        beneficiaryList = ListUtils.removeDuplicates(beneficiaryList)

        populateEditText(beneficiaryList, beneficiaryView, SingleAttributedDataUtils.getRecords().load_account)

        beneficiaryView.doOnTextChanged { text, start, before, count ->
            populatePaymentMode(allTransactionRecords)
        }
    }

    private fun populatePaymentMode(allTransactionRecords: List<CustomerMoneyDepositModel>) {
        var paymentModeList: List<String> = allTransactionRecords.stream()
            .filter { p -> p.beneficiary == beneficiaryView.text.toString()}
            .map(CustomerMoneyDepositModel::mode)
            .collect(Collectors.toList())

        if(paymentModeList.isEmpty())
            return

        paymentModeList = ListUtils.removeDuplicates(paymentModeList)
        populateEditText(paymentModeList, paymentModeView, paymentModeList[0])

        paymentModeView.doOnTextChanged { text, start, before, count ->
            populateDebitAccount(allTransactionRecords)
        }
    }

    private fun populateDebitAccount(allTransactionRecords: List<CustomerMoneyDepositModel>) {
        var debitAccountList: List<String> = allTransactionRecords.stream()
            .map(CustomerMoneyDepositModel::debitAccount)
            .collect(Collectors.toList())
        if(debitAccountList.isEmpty())
            return

        debitAccountList = ListUtils.removeDuplicates(debitAccountList)
        val debitAccountListView = findViewById<AutoCompleteTextView>(R.id.md_debit_account)
        populateEditText(debitAccountList, debitAccountListView, debitAccountList[0])

        debitAccountListView.doOnTextChanged { text, start, before, count ->
            populateCreditAccount(allTransactionRecords)
        }
    }

    private fun populateCreditAccount(allTransactionRecords: List<CustomerMoneyDepositModel>) {
        var creditAccountList: List<String> = allTransactionRecords.stream()
            .map(CustomerMoneyDepositModel::creditAccount)
            .collect(Collectors.toList())
        if(creditAccountList.isEmpty())
            return

        creditAccountList = ListUtils.removeDuplicates(creditAccountList)
        val creditAccountView = findViewById<AutoCompleteTextView>(R.id.md_credit_account)
        populateEditText(creditAccountList, creditAccountView, creditAccountList[0])

        creditAccountView.doOnTextChanged { text, start, before, count ->
            populateHandOverTo(allTransactionRecords)
        }
    }

    private fun populateHandOverTo(allTransactionRecords: List<CustomerMoneyDepositModel>) {
        var handoverToOptionsList: List<String> = allTransactionRecords.stream()
            .filter { p -> p.beneficiary == beneficiaryView.text.toString()}
            .map(CustomerMoneyDepositModel::handOverTo)
            .collect(Collectors.toList())
        if(handoverToOptionsList.isEmpty())
            return

        handoverToOptionsList = ListUtils.removeDuplicates(handoverToOptionsList)
        val handoverToListView = findViewById<AutoCompleteTextView>(R.id.md_handover_to)
        populateEditText(handoverToOptionsList, handoverToListView, handoverToOptionsList[0])
    }

    fun onClickSave(view: View) {
        val newObj = CustomerMoneyDepositModel(
            id = System.currentTimeMillis().toString(),
            beneficiary = beneficiaryView.text.toString(),
            mode = paymentModeView.text.toString(),
            debitAccount = debitAccountListView.text.toString(),
            creditAccount = creditAccountView.text.toString(),
            debitAmount = findViewById<EditText>(R.id.md_debit_amount).text.toString(),
            creditAmount = creditAmountView.text.toString(),
            handOverTo = findViewById<EditText>(R.id.md_handover_to).text.toString(),
            notes = findViewById<EditText>(R.id.md_notes).text.toString()
        )

        val saveBtn = findViewById<TextView>(R.id.md_save_button)
        Thread {
            saveBtn.text = "Saving Data..."
            CustomerMoneyDeposit.saveToServerThenLocal(newObj)
            saveBtn.text = "Save"
        }.start()

    }
    private fun populateEditText(list: List<String>, uiView: AutoCompleteTextView, selectedValue: String = "") {
        if(list == null || list.isEmpty())
            return
        val optionSet = list.filter{p -> true}.toMutableSet()
        val sortedList = optionSet.sorted()
        LogMe.log(sortedList.toString())
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.template_dropdown_entry, sortedList)
        uiView.setAdapter(adapter)
        uiView.setOnTouchListener { _, _ ->
            uiView.showDropDown()
            uiView.requestFocus()
            false
        }
        uiView.setText(selectedValue, false)
        uiView.threshold = 0
    }
}