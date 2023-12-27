package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.Loading.LoadModel
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils


class ActivityGetCustomerOrders : AppCompatActivity() {

    lateinit var containerView: View
    var uiEntriesList = mutableListOf<View>()
    lateinit var listOrders: List<GetCustomerOrderModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_order_estimates)
        AppContexts.set(this, this)
        AppUtils.logError()

        containerView = findViewById<LinearLayout>(R.id.activity_get_order_estimates__parent_view)
        findViewById<LinearLayout>(R.id.activity_get_order_estimates__order_list_container).removeAllViews()
        initializeUI(false)
    }

    private fun initializeUI(reset: Boolean) {
        listOrders = GetCustomerOrderUtils.get()
        LogMe.log(listOrders.toString())

        val avg_wt = findViewById<EditText>(R.id.get_orders_avg_wt34)
        avg_wt.doOnTextChanged { text, start, before, count ->
            val metadata = SingleAttributedDataUtils.getRecords()
            metadata.estimatedLoadAvgWt = if (avg_wt.text.toString().isEmpty()) {
                ""
            } else {
                "${avg_wt.text.toString().toInt()}"
            }
            SingleAttributedDataUtils.saveToLocal(metadata)
        }

        listOrders.forEach {
            if (reset) {
                it.orderedKg = ""
                it.orderedPc = ""
                it.calculatedKg = ""
                it.calculatedPc = ""
                it.prevDue = ""
            }
            createEstimatesView(it)
        }

        setSavedAvgWt()
        setTotalLoadOrder()
        updateTotalPc()
        updateTotalKg()
    }

    private fun setSavedAvgWt() {
        val avg_wt = findViewById<EditText>(R.id.get_orders_avg_wt34)
        avg_wt.setText(SingleAttributedDataUtils.getRecords().estimatedLoadAvgWt)
    }

    private fun setTotalLoadOrder() {
        val metadataObj = SingleAttributedDataUtils.getRecords()
        val kg = metadataObj.estimatedLoadKg + ""
        val pc = metadataObj.estimatedLoadPc + ""
        UIUtils.setUIElementValue(LoadModel.getUiElementFromOrderingPage(containerView, LoadModel::requiredKg), kg)
        UIUtils.setUIElementValue(LoadModel.getUiElementFromOrderingPage(containerView, LoadModel::requiredPc), pc)
    }

    private fun createEstimatesView(order: GetCustomerOrderModel) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_get_order_estimates__order_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_order_estimates_fragment_customer_order, null)

        val pcElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc)
        val kgElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg)
        UIUtils.setUIElementValue(entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name), order.name)
        UIUtils.setUIElementValue(pcElement, order.orderedPc)
        UIUtils.setUIElementValue(kgElement, order.orderedKg)

        pcElement.doOnTextChanged { text, start, before, count ->
            order.orderedPc = pcElement.text.toString()
            GetCustomerOrderUtils.updateObj(order)
            updateTotalPc()
        }

        kgElement.doOnTextChanged { text, start, before, count ->
            order.orderedKg = kgElement.text.toString()
            GetCustomerOrderUtils.updateObj(order)
            updateTotalKg()
        }

        uiEntriesList.add(entry)
        listContainer.addView(entry)
    }

    private fun getJustTheNumber(str: String): Int {
        val trimmedStr = str.replace(" ", "").replace("-", "")
        if (trimmedStr.isEmpty())
            return 0
        return NumberUtils.getIntOrZero(trimmedStr)
    }

    private fun updateTotalKg() {
        var sum = 0
        GetCustomerOrderUtils.get().forEach {
            sum += getJustTheNumber(it.orderedKg)
        }
        UIUtils.setUIElementValue(containerView.findViewById(R.id.activity_get_order_estimates__total_kg), "$sum kg")
    }

    private fun updateTotalPc() {
        var sum = 0
        GetCustomerOrderUtils.get().forEach {
            sum += getJustTheNumber(it.orderedPc)
        }
        UIUtils.setUIElementValue(containerView.findViewById(R.id.activity_get_order_estimates__total_pc), "$sum pc")
    }

    private fun getCustomerNamesAsStringList(): List<String> {
        val namesList = mutableListOf<String>()

        CustomerKYC.get().forEach {
            namesList.add(it.nameEng)
        }
        return namesList
    }

    fun onClickGoToFinalizeOrdersPage(view: View) {
        if (SingleAttributedDataUtils.getRecords().estimatedLoadAvgWt.isEmpty()) {
            Toast.makeText(this, "Please enter avg wt.", Toast.LENGTH_LONG).show()
            return
        }
        val switchActivityIntent = Intent(this, GetOrdersFinalize::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    fun onClickGoToMakeListPage(view: View) {
        if (SingleAttributedDataUtils.getRecords().estimatedLoadAvgWt.isEmpty()) {
            Toast.makeText(this, "Please enter avg wt.", Toast.LENGTH_LONG).show()
            return
        }
        val switchActivityIntent = Intent(this, OrdersMakeList::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }

    fun onClickClearButton(view: View) {
//        GetCustomerOrders.saveToLocal(mutableListOf())
        initializeUI(true)
    }
}