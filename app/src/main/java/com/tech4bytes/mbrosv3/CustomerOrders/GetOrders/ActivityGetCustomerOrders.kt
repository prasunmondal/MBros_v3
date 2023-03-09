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
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Loading.LoadModel
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe


class ActivityGetCustomerOrders : AppCompatActivity() {

    lateinit var containerView: View
    var uiEntriesList = mutableListOf<View>()
    lateinit var listOrders: List<GetCustomerOrders>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_order_estimates)
        AppContexts.set(this, this)
        AppUtils.logError()

        containerView = findViewById<LinearLayout>(R.id.activity_get_order_estimates__parent_view)

        listOrders = GetCustomerOrders.get()
        LogMe.log(listOrders.toString())

        listOrders.forEach {
            createEstimatesView(it)
        }

        setSavedAvgWt()
        setTotalLoadOrder()
        updateTotalPc()
        updateTotalKg()
    }

    private fun setSavedAvgWt() {
        val avg_wt = findViewById<EditText>(R.id.get_orders_avg_wt)
        avg_wt.setText(SingleAttributedData.getRecords().estimatedLoadAvgWt)
    }

    private fun setTotalLoadOrder() {
        val metadataObj = SingleAttributedData.getRecords()
        val kg = metadataObj.estimatedLoadKg + ""
        val pc = metadataObj.estimatedLoadPc + ""
        UIUtils.setUIElementValue(LoadModel.getUiElementFromOrderingPage(containerView, LoadModel::requiredKg), kg)
        UIUtils.setUIElementValue(LoadModel.getUiElementFromOrderingPage(containerView, LoadModel::requiredPc), pc)
    }

    private fun createEstimatesView(order: GetCustomerOrders) {
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
            GetCustomerOrders.updateObj(order)
            updateTotalPc()
        }

        kgElement.doOnTextChanged { text, start, before, count ->
            order.orderedKg = kgElement.text.toString()
            GetCustomerOrders.updateObj(order)
            updateTotalKg()
        }

        val avg_wt = findViewById<EditText>(R.id.get_orders_avg_wt)
        avg_wt.doOnTextChanged { text, start, before, count ->
            val metadata = SingleAttributedData.getRecords()
            metadata.estimatedLoadAvgWt = if(avg_wt.text.toString().isEmpty()) {
                ""
            } else { "${avg_wt.text.toString().toInt()}" }
            SingleAttributedData.saveToLocal(metadata)
        }

        uiEntriesList.add(entry)
        listContainer.addView(entry)
    }

    private fun getJustTheNumber(str: String): Int {
        val trimmedStr = str.replace(" ","").replace("-","")
        if(trimmedStr.isEmpty())
            return 0
        return trimmedStr.toInt()
    }

    private fun updateTotalKg() {
        var sum = 0
        GetCustomerOrders.get().forEach {
            sum += getJustTheNumber(it.orderedKg)
        }
        UIUtils.setUIElementValue(containerView.findViewById(R.id.activity_get_order_estimates__total_kg), "$sum kg")
    }

    private fun updateTotalPc() {
        var sum = 0
        GetCustomerOrders.get().forEach {
            sum += getJustTheNumber(it.orderedPc)
        }
        UIUtils.setUIElementValue(containerView.findViewById(R.id.activity_get_order_estimates__total_pc), "$sum pc")
    }

    private fun getCustomerNamesAsStringList(): List<String> {
        val namesList = mutableListOf<String>()

        CustomerKYC.getAllCustomers().forEach {
            namesList.add(it.getDisplayName())
        }
        return namesList
    }

    fun onClickSaveBtn(view: View) {
        Toast.makeText(this, "Saving Data", Toast.LENGTH_SHORT).show()
        GetCustomerOrders.deleteAll()
        GetCustomerOrders.save()

        val metadataObj = SingleAttributedData.getRecords()
        metadataObj.estimatedLoadPc = UIUtils.getUIElementValue(LoadModel.getUiElementFromOrderingPage(view, LoadModel::requiredPc))
        metadataObj.estimatedLoadKg = UIUtils.getUIElementValue(LoadModel.getUiElementFromOrderingPage(view, LoadModel::requiredKg))

        SingleAttributedData.save(metadataObj)
        Toast.makeText(this, "Data save complete!", Toast.LENGTH_LONG).show()
    }

    fun onClickGoToFinalizeOrdersPage(view: View) {
        if(SingleAttributedData.getRecords().estimatedLoadAvgWt.isEmpty()) {
            Toast.makeText(this, "Please enter avg wt.", Toast.LENGTH_LONG).show()
            return
        }
        val switchActivityIntent = Intent(this, GetOrdersFinalize::class.java)
        startActivity(switchActivityIntent)
    }
}