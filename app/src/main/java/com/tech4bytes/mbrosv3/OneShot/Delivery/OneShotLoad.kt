package com.tech4bytes.mbrosv3.OneShot.Delivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class OneShotLoad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_load)
        AppContexts.set(this)

        AppUtils.logError()
        updateUIFromObj(true)
    }

    private fun updateUIFromObj(useCache: Boolean) {
        val obj = SingleAttributedData.getRecords(useCache)
        val companyName = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_name)
        val branch = findViewById<AutoCompleteTextView>(R.id.one_shot_load_company_branch)
        val account = findViewById<AutoCompleteTextView>(R.id.one_shot_load_money_account)
        val loadingArea = findViewById<AutoCompleteTextView>(R.id.one_shot_load_loading_area)
        val extraCashProvider = findViewById<TextInputEditText>(R.id.one_shot_load_extra_expense_provided)
        val farmRate = findViewById<TextInputEditText>(R.id.one_shot_load_farm_rate)

        companyName.setText(obj.load_companyName)
        branch.setText(obj.load_branch)
        account.setText(obj.load_account)
        loadingArea.setText(obj.load_area)
        extraCashProvider.setText(obj.extra_cash_given)
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

        if(companyName.isNotEmpty()) obj.load_companyName = companyName
        if(branch.isNotEmpty()) obj.load_branch = branch
        if(account.isNotEmpty()) obj.load_account = account
        if(loadingArea.isNotEmpty()) obj.load_area = loadingArea

        if(extraCashProvider.isNotEmpty()) obj.extra_cash_given = extraCashProvider
        if(farmRate.isNotEmpty()) obj.finalFarmRate = farmRate

        SingleAttributedData.saveToLocal(obj)
    }

    fun onClickOneShotLoadSaveBtn(view: View) {
        updateObjFromUI()
        SingleAttributedData.save(SingleAttributedData.getRecords())
    }

    fun onClickOneShotLoadRefreshBtn(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
    }
}