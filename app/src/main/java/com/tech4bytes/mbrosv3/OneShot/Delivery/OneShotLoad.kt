package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.stream.Collectors


class OneShotLoad : AppCompatActivity() {

    var isDataFresh: Boolean = true
    lateinit var oslSaveBtn: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_load)
        AppContexts.set(this)
        AppUtils.logError()
        initializeVariables()
        initializeUI()
    }

    private fun initializeVariables() {
        oslSaveBtn = findViewById(R.id.osl_save_btn)
    }

    fun initializeUI() {
        setDecors()
        populateDropDowns()
        setListeners()
        updateUIFromObj()
        markDataFresh(true, true)
    }

    private fun setListeners() {
        val initialFarmRate = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate)
        val finalFarmRate = findViewById<TextInputEditText>(R.id.osl_final_farm_rate)
        val inHandCash = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided)
        val loadingCompany = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val loadingCompanyBranch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val loadingCompanyArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)
        val loadingCompanyMoneyAccount = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)

        initialFarmRate.addTextChangedListener {
            markDataFresh(false)
            finalFarmRate.setText(((NumberUtils.getIntOrZero(initialFarmRate.text.toString()) - 10)).toString())
        }
        finalFarmRate.addTextChangedListener { markDataFresh(false) }
        inHandCash.addTextChangedListener { markDataFresh(false) }
        loadingCompany.addTextChangedListener { markDataFresh(false) }
        loadingCompanyBranch.addTextChangedListener { markDataFresh(false) }
        loadingCompanyArea.addTextChangedListener { markDataFresh(false) }
        loadingCompanyMoneyAccount.addTextChangedListener { markDataFresh(false) }
    }

    private fun populateDropDowns() {
        populateOptionsCompanyName(false)
        populateOptionsCompanyBranch(false)
        populateOptionsArea(false)
    }

    private fun setDecors() {
        val finalFarmRateContainer = findViewById<TextInputLayout>(R.id.osl_final_farm_rate_container)
        finalFarmRateContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        val deliveryBasePriceContainer = findViewById<TextInputLayout>(R.id.one_shot_load_farm_rate_container)
        deliveryBasePriceContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        val inHandContainer = findViewById<TextInputLayout>(R.id.osl_in_hand_cash_container)
        inHandContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
    }

    private fun populateOptionsCompanyName(showDropdown: Boolean) {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {d -> d.companyName.isNotEmpty()}
            .map(CompanyLoadMap::companyName)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setAdapter(arrayAdapter)
        if(arrayList.isNotEmpty()) autoCompleteTextView.setText(arrayList[0])
        if (showDropdown) autoCompleteTextView.showDropDown()
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            populateOptionsCompanyBranch(true)
            populateOptionsMoneyAccount(false)
        }
    }

    private fun populateOptionsCompanyBranch(showDropdown: Boolean) {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {c -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString() == c.companyName}
            .filter {d -> d.branch.isNotEmpty()}
            .map(CompanyLoadMap::branch)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setAdapter(arrayAdapter)
        if (showDropdown) autoCompleteTextView.showDropDown()
        if(arrayList.isNotEmpty()) autoCompleteTextView.setText(arrayList[0])
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            populateOptionsArea(true)
        }
    }
//
    private fun populateOptionsArea(showDropdown: Boolean) {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {c -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString() == c.companyName}
            .filter {d -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch).text.toString() == d.branch}
            .filter {d -> d.area.isNotEmpty()}
            .map(CompanyLoadMap::area)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setAdapter(arrayAdapter)
        if(arrayList.isNotEmpty()) autoCompleteTextView.setText(arrayList[0])
        if (showDropdown) autoCompleteTextView.showDropDown()
        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
    }

    private fun populateOptionsMoneyAccount(showDropdown: Boolean) {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)
        val arrayList: List<String> = CompanyLoadMap.get().stream()
            .filter {c -> findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString() == c.companyName}
            .filter {d -> d.moneyAccount.isNotEmpty()}
            .map(CompanyLoadMap::moneyAccount)
            .collect(Collectors.toSet()).toList()
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayList)
        autoCompleteTextView.setAdapter(arrayAdapter)

        if(arrayList.isNotEmpty()) autoCompleteTextView.setText(arrayList[0])
        if (showDropdown) autoCompleteTextView.showDropDown()

        autoCompleteTextView.setOnTouchListener { _, _ ->
            autoCompleteTextView.showDropDown()
            autoCompleteTextView.requestFocus()
            false
        }
    }

    private fun updateUIFromObj(useCache: Boolean = true) {
        val obj = SingleAttributedData.getRecords(useCache)
        val companyName = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val branch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val account = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)
        val loadingArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)
        val extraCashProvider = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided)
        val deliveryBasePrice = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate)
        val farmRate = findViewById<TextInputEditText>(R.id.osl_final_farm_rate)

        companyName.setText(obj.load_companyName)
        branch.setText(obj.load_branch)
        account.setText(obj.load_account)
        loadingArea.setText(obj.load_area)
        extraCashProvider.setText(obj.extra_cash_given)
        deliveryBasePrice.setText((NumberUtils.getIntOrZero(obj.finalFarmRate) + NumberUtils.getIntOrZero(obj.bufferRate) + 10).toString())
        farmRate.setText(obj.finalFarmRate)
    }

    private fun updateObjFromUI() {
        val obj = SingleAttributedData.getRecords()
        val companyName = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name).text.toString()
        val branch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch).text.toString()
        val account = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account).text.toString()
        val loadingArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area).text.toString()
        val extraCashProvider = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided).text.toString()
        val farmRate = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate).text.toString()
        val finalFarmRate = findViewById<TextInputEditText>(R.id.osl_final_farm_rate).text.toString()

        obj.load_companyName = companyName
        obj.load_branch = branch
        obj.load_account = account
        obj.load_area = loadingArea
        obj.finalFarmRate = finalFarmRate
        obj.bufferRate = (NumberUtils.getIntOrZero(farmRate) - 10 - NumberUtils.getIntOrZero(finalFarmRate)).toString()

        obj.extra_cash_given = extraCashProvider
        SingleAttributedData.saveToLocal(obj)
    }

    fun onClickOneShotLoadSaveBtn(view: View) {
        updateObjFromUI()
        Thread {
            runOnUiThread {
                oslSaveBtn.isEnabled = false
                oslSaveBtn.alpha = .5f
                oslSaveBtn.isClickable = false;
            }
            SingleAttributedData.save(SingleAttributedData.getRecords())
            runOnUiThread {
                markDataFresh(true)
                Toast.makeText(this, "Data Saved!", Toast.LENGTH_LONG).show()
                oslSaveBtn.isEnabled = true
                oslSaveBtn.alpha = 1.0f;
                oslSaveBtn.isClickable = true;
            }
        }.start()
    }

    fun onClickOneShotLoadRefreshBtn(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
    }

    private fun markDataFresh(isDataFresh: Boolean, forceUpdate: Boolean = false) {
        if (!forceUpdate && this.isDataFresh == isDataFresh) {
            return
        }

        this.isDataFresh = isDataFresh
        val oslRateInnerContainerElement = findViewById<LinearLayout>(R.id.osl_rate_inner_container)
        val oslRateOuterContainerElement = findViewById<LinearLayout>(R.id.osl_rate_outer_container)
        val oslCompanyDetailsOuterContainerElement = findViewById<LinearLayout>(R.id.osl_company_details_outer_container)

        oslRateOuterContainerElement.backgroundTintList = if (isDataFresh) this.resources.getColorStateList(R.color.osl_data_status_fresh_light) else this.resources.getColorStateList(R.color.osl_data_status_stale_light)
        oslCompanyDetailsOuterContainerElement.backgroundTintList = if (isDataFresh) this.resources.getColorStateList(R.color.osl_data_status_fresh_light) else this.resources.getColorStateList(R.color.osl_data_status_stale_light)
        oslRateInnerContainerElement.backgroundTintList = if (isDataFresh) this.resources.getColorStateList(R.color.osl_data_status_fresh_dark) else this.resources.getColorStateList(R.color.osl_data_status_stale_dark)
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
        val finalFarmRate = findViewById<TextInputEditText>(R.id.osl_final_farm_rate)

        extraCashProvided.text!!.clear()
        farmRate.text!!.clear()
        finalFarmRate.text!!.clear()
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }

    fun onClickExitButton(view: View) {
        this.finishAffinity()
    }
}