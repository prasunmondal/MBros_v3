package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import java.util.stream.Collectors


class OneShotLoad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_load)
        AppContexts.set(this)

        AppUtils.logError()
        populateOptionsCompanyName()
        populateOptionsCompanyBranch()
        populateOptionsArea()
        updateUIFromObj(false)
    }

    private fun populateOptionsCompanyName() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {d -> d.companyName.isNotEmpty()}
            .map(CompanyLoadMap::companyName)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            populateOptionsCompanyBranch()
            populateOptionsMoneyAccount()
        }
        autoCompleteTextView.setAdapter(arrayAdapter)
    }

    private fun populateOptionsCompanyBranch() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {c -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString() == c.companyName}
            .filter {d -> d.branch.isNotEmpty()}
            .map(CompanyLoadMap::branch)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            populateOptionsArea()
        }
        autoCompleteTextView.setAdapter(arrayAdapter)
    }
//
    private fun populateOptionsArea() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {c -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString() == c.companyName}
            .filter {d -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch).text.toString() == d.branch}
            .filter {d -> d.area.isNotEmpty()}
            .map(CompanyLoadMap::area)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
        autoCompleteTextView.setAdapter(arrayAdapter)
    }

    private fun populateOptionsMoneyAccount() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {c -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString() == c.companyName}
            .filter {d -> d.moneyAccount.isNotEmpty()}
            .map(CompanyLoadMap::moneyAccount)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
        autoCompleteTextView.setAdapter(arrayAdapter)
    }

    private fun updateUIFromObj(useCache: Boolean) {
        val obj = SingleAttributedData.getRecords(useCache)
        val companyName = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val branch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val account = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)
        val loadingArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)
        val extraCashProvider = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided)
        val farmRate = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate)
        val bufferPrice = findViewById<TextInputEditText>(R.id.one_shot_load_buffer_price)

        companyName.setText(obj.load_companyName)
        branch.setText(obj.load_branch)
        account.setText(obj.load_account)
        loadingArea.setText(obj.load_area)
        extraCashProvider.setText(obj.extra_cash_given)
        farmRate.setText(obj.finalFarmRate)
        bufferPrice.setText(obj.bufferRate)
    }

    private fun updateObjFromUI() {
        val obj = SingleAttributedData.getRecords()
        val companyName = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString()
        val branch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch).text.toString()
        val account = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account).text.toString()
        val loadingArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area).text.toString()
        val extraCashProvider = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided).text.toString()
        val farmRate = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate).text.toString()
        val bufferPrice = findViewById<TextInputEditText>(R.id.one_shot_load_buffer_price).text.toString()

        obj.load_companyName = companyName
        obj.load_branch = branch
        obj.load_account = account
        obj.load_area = loadingArea
        obj.bufferRate = bufferPrice

        obj.extra_cash_given = extraCashProvider
        obj.finalFarmRate = farmRate

        SingleAttributedData.saveToLocal(obj)
    }

    fun onClickOneShotLoadSaveBtn(view: View) {
        updateObjFromUI()
        SingleAttributedData.save(SingleAttributedData.getRecords())
    }

    fun onClickOneShotLoadRefreshBtn(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
    }

    fun onClickClearCompanyLoadingDetails(view: View) {
        val companyName = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val branch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val account = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)
        val loadingArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)

        companyName.text!!.clear()
        branch.text!!.clear()
        account.text!!.clear()
        loadingArea.text!!.clear()
    }

    fun onClickClearAmountsDetails(view: View) {
        val extraCashProvided = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided)
        val farmRate = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate)
        val bufferPrice = findViewById<AutoCompleteTextView>(R.id.one_shot_load_buffer_price)

        extraCashProvided.text!!.clear()
        farmRate.text!!.clear()
        bufferPrice.text!!.clear()
    }
}