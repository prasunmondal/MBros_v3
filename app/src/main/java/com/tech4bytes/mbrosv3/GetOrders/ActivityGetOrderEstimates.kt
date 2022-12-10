package com.tech4bytes.mbrosv3.GetOrders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.prasunmondal.postjsontosheets.clients.delete.Delete
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe


class ActivityGetOrderEstimates : AppCompatActivity() {

    lateinit var containerView: View
    var uiEntriesList = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_order_estimates)
        AppContexts.set(this, this)
        containerView = findViewById<LinearLayout>(R.id.activity_get_order_estimates__order_list_container)

        populateCustomerList()
    }

    private fun createEstimatesView(customerName: String) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_get_order_estimates__order_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_order_estimates_fragment_customer_order, null)

        entry.findViewById<TextView>(R.id.fragment_customer_order_name).text = customerName

        val deleteBtn = entry.findViewById<ImageButton>(R.id.fragment_customer_order_delete_record_button)
        deleteBtn.setOnClickListener {
            uiEntriesList.remove(entry)
            listContainer.removeView(entry)
        }

        uiEntriesList.add(entry)
        listContainer.addView(entry)

    }

    private fun getCustomerNamesAsStringList(): List<String> {
        val namesList = mutableListOf<String>()

        CustomerKYC.getAllCustomers().forEach {
            namesList.add(it.getDisplayName())
        }
        return namesList
    }

    @SuppressLint("ClickableViewAccessibility")
    fun populateCustomerList() {
        val dropDown = findViewById<MaterialAutoCompleteTextView>(R.id.activity_get_order_estimates__customer_selection_dropdown)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(AppContexts.get(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, getCustomerNamesAsStringList())
        (dropDown as MaterialAutoCompleteTextView).setAdapter(adapter)

        dropDown.setOnTouchListener { _, _ ->
                dropDown.showDropDown()
                dropDown.requestFocus()
                false
            }

        dropDown.setOnItemClickListener { parent, arg1, position, arg3 ->
            val item = parent.getItemAtPosition(position)
            LogMe.log("Selected Customer: $item")
            if(!isOnList(item.toString())) {
                createEstimatesView(item.toString())
            } else {
                Toast.makeText(this, "Already on list", Toast.LENGTH_SHORT).show()
            }
            dropDown.text.clear()
        }
    }

    fun deleteAll() {
        Delete.builder()
            .scriptId(ProjectConfig.dBServerScriptURL)
            .sheetId(ProjectConfig.DB_SHEET_ID)
            .tabName(GetOrdersConfig.SHEET_TAB_NAME)
            .build().execute()
    }

    fun createNGetObjectsFromUI(): List<OrderEstimateModel> {
        val list = mutableListOf<OrderEstimateModel>()
        uiEntriesList.forEach {
            val seqNo = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_sl_no))
            val name = UIUtils.getUIElementValue(it.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name))
            val pc = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc))
            val kg = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg))
            val rate = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_rate))
            val obj = OrderEstimateModel(id = System.currentTimeMillis().toString(),
                name = name,
                seqNo = seqNo,
                estimatePc = pc,
                estimateKg = kg,
                rate = rate,
                due = "0")
            list.add(obj)
        }
        return list
    }

    private fun isOnList(name: String): Boolean {
        uiEntriesList.forEach {
            val nameFromList = UIUtils.getUIElementValue(it.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name))
            if(nameFromList == name)
                return true
        }
        return false
    }

    private fun saveToServer() {
        createNGetObjectsFromUI().forEach {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.DB_SHEET_ID)
                .tabName(GetOrdersConfig.SHEET_TAB_NAME)
                .dataObject(it as Any)
                .build().execute()
        }
    }

    fun onClickSaveBtn(view: View) {
        deleteAll()
        saveToServer()
    }
}