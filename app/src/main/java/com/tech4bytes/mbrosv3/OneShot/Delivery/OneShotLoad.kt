package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Language.English.EnglishUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.stream.Collectors


class OneShotLoad : AppCompatActivity() {

    private var isDataFresh: Boolean = true
    private lateinit var oslSaveBtn: MaterialButton
    private lateinit var initialFarmRate: TextInputEditText
    private lateinit var finalFarmRate: TextInputEditText
    private lateinit var inHandCash: TextInputEditText
//    private lateinit var dropdownContainer: LinearLayout
//    private lateinit var freeTextView: TextView
//    private lateinit var freeTextOkBtn: Button
    private lateinit var companyLabel2: AutoCompleteTextView
    private lateinit var companyBranch2: AutoCompleteTextView
    private lateinit var companyArea2: AutoCompleteTextView
    private lateinit var companyAccount2: AutoCompleteTextView
    private lateinit var labour2: AppCompatImageView

    private var labour2Enabled = false

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
        initialFarmRate = findViewById(R.id.one_shot_load_farm_rate)
        finalFarmRate = findViewById(R.id.osl_final_farm_rate)
        inHandCash = findViewById(R.id.one_shot_load_extra_expense_provided)
//        dropdownContainer = findViewById(R.id.osl_dropdown_container)
//        freeTextView = findViewById(R.id.osl_editText_freeText)
//        freeTextOkBtn = findViewById(R.id.osl_btn_saveFreeText)
        companyLabel2 = findViewById(R.id.osl_label_company_name_2)
        companyBranch2 = findViewById(R.id.osl_label_branch_name_2)
        companyArea2 = findViewById(R.id.osl_label_area_name_2)
        companyAccount2 = findViewById(R.id.osl_label_money_account_name_2)
        labour2 = findViewById(R.id.addLabour2)
    }

    private fun initializeUI() {
        setDecors()
        setUIValues(false)
        setListeners()
        updateUIFromObj()
        processLabour2PayElements()
        updateAllPays()
        markDataFresh(true, true)
    }

    private fun setUIValues(fromUI: Boolean = true) {
        val data = SingleAttributedData.getRecords()
        val companyName = if (fromUI) companyLabel2.text.toString() else data.load_companyName
        val branchName = if (fromUI) companyBranch2.text.toString() else data.load_branch
        val areaName = if (fromUI) companyArea2.text.toString() else data.load_area
        val account = if (fromUI) companyAccount2.text.toString() else data.load_account

        showOptions(getCompanyNames(), companyLabel2, companyName)
        showOptions(getBranchNames(companyName), companyBranch2, branchName)
        showOptions(getLoadAreas(companyName, branchName), companyArea2, areaName)
        showOptions(getAccountName(companyName), companyAccount2, account)
    }

    private fun showOptions(list: List<String>, uiView: AutoCompleteTextView, selectedValue: String = "", filter: String = "") {
        val optionSet = list.filter { p -> p.isNotEmpty() }.toMutableSet()
        val sortedList = optionSet.sorted()
        LogMe.log(sortedList.toString())
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.template_dropdown_entry, sortedList)
        uiView.setAdapter(adapter)
        uiView.setOnTouchListener { _, _ ->
            uiView.showDropDown()
            uiView.requestFocus()
            false
        }
        uiView.doOnTextChanged { text, start, before, count ->
            markDataFresh(false)
        }
        uiView.setOnItemClickListener { adapterView, view, i, l ->
            LogMe.log("Updating dropdowns")
            setUIValues(true)
        }
        uiView.setText(selectedValue, false)
        uiView.threshold = 0
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(oslSaveBtn.windowToken, 0)
    }

    private fun setListeners() {
        initialFarmRate.addTextChangedListener {
            markDataFresh(false)
            finalFarmRate.setText(((NumberUtils.getIntOrZero(initialFarmRate.text.toString()) - 10)).toString())
        }
        finalFarmRate.addTextChangedListener { markDataFresh(false) }
        inHandCash.addTextChangedListener { markDataFresh(false) }
    }

    private fun setDecors() {
        val finalFarmRateContainer = findViewById<TextInputLayout>(R.id.osl_final_farm_rate_container)
        finalFarmRateContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        val deliveryBasePriceContainer = findViewById<TextInputLayout>(R.id.one_shot_load_farm_rate_container)
        deliveryBasePriceContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        val inHandContainer = findViewById<TextInputLayout>(R.id.osl_in_hand_cash_container)
        inHandContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
//        dropdownContainer.visibility = View.GONE
    }

    private fun getCompanyNames(): List<String> {
        return CompanyLoadMap.get().stream()
            .filter { d -> d.companyName.trim().isNotEmpty() }
            .map(CompanyLoadMap::companyName)
            .collect(Collectors.toSet()).toList()
    }

    private fun getBranchNames(companyName: String): List<String> {
        return CompanyLoadMap.get().stream()
            .filter { c -> companyName == c.companyName }
            .filter { d -> d.branch.trim().isNotEmpty() }
            .map(CompanyLoadMap::branch)
            .collect(Collectors.toSet()).sorted().toList()
    }

    private fun getLoadAreas(companyName: String, branchName: String): List<String> {
        return CompanyLoadMap.get().stream()
            .filter { c -> companyName == c.companyName }
            .filter { d -> branchName == d.branch }
            .filter { d -> d.area.trim().isNotEmpty() }
            .map(CompanyLoadMap::area)
            .collect(Collectors.toSet()).sorted().toList()
    }

    private fun getAccountName(companyName: String): List<String> {
        return CompanyLoadMap.get().stream()
            .filter { c -> companyName == c.companyName }
            .filter { d -> d.moneyAccount.trim().isNotEmpty() }
            .map(CompanyLoadMap::moneyAccount)
            .collect(Collectors.toSet()).toList()
    }

    private fun updateUIFromObj(useCache: Boolean = true) {
        val obj = SingleAttributedData.getRecords(useCache)
        val deliveryBasePrice = initialFarmRate
        inHandCash.setText(obj.extra_cash_given)
        deliveryBasePrice.setText(DeliveryCalculations.getBaseDeliveryPrice(obj.finalFarmRate, obj.bufferRate).toString())
        finalFarmRate.setText(obj.finalFarmRate)
    }

    private fun updateObjFromUI() {
        val obj = SingleAttributedData.getRecords()
        val companyName = companyLabel2.text.toString()
        val branch = companyBranch2.text.toString()
        val account = companyAccount2.text.toString()
        val loadingArea = companyArea2.text.toString()
        val extraCashProvider = inHandCash.text.toString()
        val farmRate = initialFarmRate.text.toString()
        val finalFarmRate = finalFarmRate.text.toString()

        obj.load_companyName = companyName
        obj.load_branch = branch
        obj.load_account = account
        obj.load_area = loadingArea
        obj.finalFarmRate = finalFarmRate
        obj.bufferRate = (NumberUtils.getIntOrZero(farmRate) - 10 - NumberUtils.getIntOrZero(finalFarmRate)).toString()

        obj.extra_cash_given = extraCashProvider
        SingleAttributedData.saveToLocal(obj)
    }

    fun convertToWordCase(str: String): String {
        return EnglishUtils.toWordCase(str)
    }

    private fun updateCase() {
        companyLabel2.setText(convertToWordCase(companyLabel2.text.toString()))
        companyBranch2.setText(convertToWordCase(companyBranch2.text.toString()))
        companyArea2.setText(convertToWordCase(companyArea2.text.toString()))
        companyAccount2.setText(convertToWordCase(companyAccount2.text.toString()))
    }

    fun onClickOneShotLoadSaveBtn(view: View) {
        updateCase()
        hideKeyboard()
        updateObjFromUI()
        SingleAttributedData.saveToLocal(SingleAttributedData.getRecords())
        Thread {
            runOnUiThread {
                oslSaveBtn.isEnabled = false
                oslSaveBtn.alpha = .5f
                oslSaveBtn.isClickable = false
            }
            SingleAttributedData.save(SingleAttributedData.getRecords())
            SingleAttributedData.getRecords(false)
            runOnUiThread {
                markDataFresh(true)
                Toast.makeText(this, "Data Saved!", Toast.LENGTH_LONG).show()
                oslSaveBtn.isEnabled = true
                oslSaveBtn.alpha = 1.0f
                oslSaveBtn.isClickable = true
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

    fun onClickClearAmountsDetails(view: View) {
        inHandCash.text!!.clear()
        initialFarmRate.text!!.clear()
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

    fun onClickAddLabour2(view: View) {
        labour2Enabled = !labour2Enabled
        processLabour2PayElements()
        updateAllPays()
    }

    fun clickIncreasePay(view: View) {
        incrementPay(50)
    }

    fun clickDecreasePay(view: View) {
        incrementPay(-50)
    }

    private fun incrementPay(i: Int) {
        val extraPay = i + NumberUtils.getIntOrZero(findViewById<TextView>(R.id.osl_driver_extra_pay).text.toString().replace("+", ""))
        findViewById<TextView>(R.id.osl_driver_extra_pay).text = "+$extraPay"
        findViewById<TextView>(R.id.osl_labour1_extra_pay).text = "+$extraPay"
        findViewById<TextView>(R.id.osl_labour2_extra_pay).text = "+$extraPay"
        updateAllPays()
    }

    private fun updateAllPays() {
        val driverPay = updateTotalPay(R.id.osl_driver_base_pay, R.id.osl_driver_extra_pay, R.id.osl_driver_total_pay, R.id.osl_driver_pay_container)
        val labour1Pay = updateTotalPay(R.id.osl_labour1_base_pay, R.id.osl_labour1_extra_pay, R.id.osl_labour1_total_pay, R.id.osl_labour1_pay_container)
        val labour2Pay = updateTotalPay(R.id.osl_labour2_base_pay, R.id.osl_labour2_extra_pay, R.id.osl_labour2_total_pay, R.id.osl_labour2_pay_container)

        val extraPay = NumberUtils.getIntOrZero(findViewById<TextView>(R.id.osl_driver_extra_pay).text.toString().replace("+", ""))
        if(extraPay == 0) {
            findViewById<LinearLayout>(R.id.osl_driver_extra_pay_container).visibility = View.GONE
            findViewById<LinearLayout>(R.id.osl_labour1_extra_pay_container).visibility = View.GONE
            findViewById<LinearLayout>(R.id.osl_labour2_extra_pay_container).visibility = View.GONE
        } else {
            findViewById<LinearLayout>(R.id.osl_driver_extra_pay_container).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.osl_labour1_extra_pay_container).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.osl_labour2_extra_pay_container).visibility = View.VISIBLE
        }
        findViewById<TextView>(R.id.osl_total_salary).text = "₹ " + (driverPay + labour1Pay + labour2Pay).toString()
    }

    private fun processLabour2PayElements() {
        val labour2PayContainer = findViewById<LinearLayout>(R.id.osl_labour2_pay_container)

        LogMe.log("Labour 2 enabled? $labour2Enabled")
        if(labour2Enabled) {
            labour2PayContainer.visibility = View.VISIBLE
            labour2.setColorFilter(ContextCompat.getColor(AppContexts.get(), R.color.osl_person_avatar_active))
            labour2.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_outline_for_avatar))
        } else {
            labour2PayContainer.visibility = View.INVISIBLE
            labour2.clearColorFilter()
            labour2.setBackgroundDrawable(null)
        }
    }

    fun updateTotalPay(basePayUI: Int, extraPayUI: Int, totalPayUI: Int, payView: Int): Int {
        val isPaid = findViewById<LinearLayout>(payView).isVisible
        val basePay = NumberUtils.getIntOrZero(findViewById<TextView>(basePayUI).text.toString())
        val extraPay = NumberUtils.getIntOrZero(findViewById<TextView>(extraPayUI).text.toString().replace("+", ""))
        val totalPay = basePay + extraPay
        val totalPayUI = findViewById<TextView>(totalPayUI)
        totalPayUI.text = totalPay.toString()

        return if(isPaid) totalPay else 0
    }
}