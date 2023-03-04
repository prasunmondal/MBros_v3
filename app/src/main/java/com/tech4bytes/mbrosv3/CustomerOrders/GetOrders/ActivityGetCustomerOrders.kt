package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_order_estimates)
        AppContexts.set(this, this)
        AppUtils.logError()

        containerView = findViewById<LinearLayout>(R.id.activity_get_order_estimates__parent_view)

//        populateCustomerList()

        CustomerKYC.getAllCustomers().forEach { masterList ->
            var isInOrderList = false
            GetCustomerOrders.get().forEach { orderList ->
                if(masterList.nameEng == orderList.name) {
                    createEstimatesView(orderList)
                    isInOrderList = true
                }
            }
            if(!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                createEstimatesView(masterList.nameEng)
            }
        }
        setTotalLoadOrder()
        updateTotalPc()
        updateTotalKg()
    }

    private fun setTotalLoadOrder() {
        val metadataObj = SingleAttributedData.getRecords()
        val kg = metadataObj.estimatedLoadKg + ""
        val pc = metadataObj.estimatedLoadPc + ""
        UIUtils.setUIElementValue(this, LoadModel.getUiElementFromOrderingPage(containerView, LoadModel::requiredKg), kg)
        UIUtils.setUIElementValue(this, LoadModel.getUiElementFromOrderingPage(containerView, LoadModel::requiredPc), pc)
    }

    private fun createEstimatesView(customerName: String) {
        createEstimatesView(GetCustomerOrders(name = customerName))
    }

    private fun createEstimatesView(order: GetCustomerOrders) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_get_order_estimates__order_list_container)
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_order_estimates_fragment_customer_order, null)

        val pcElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc)
        val kgElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg)
        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_sl_no), order.seqNo)
        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name), order.name)
        UIUtils.setUIElementValue(this, pcElement, order.estimatePc)
        UIUtils.setUIElementValue(this, kgElement, order.estimateKg)
        UIUtils.setUIElementValue(this, entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_rate), order.rate)

        pcElement.doOnTextChanged { text, start, before, count ->
            updateTotalPc()
        }

        kgElement.doOnTextChanged { text, start, before, count ->
            updateTotalKg()
        }

        val deleteBtn = entry.findViewById<ImageButton>(R.id.fragment_customer_order_delete_record_button)
        deleteBtn.setOnClickListener {
            uiEntriesList.remove(entry)
            listContainer.removeView(entry)
            updateTotalPc()
            updateTotalKg()
        }

        uiEntriesList.add(entry)
        listContainer.addView(entry)
    }

    private fun updateTotalKg() {
        var sum = 0.0
        uiEntriesList.forEach {
            val kg = "0${UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg))}"
            sum += kg.toDouble()
        }
        LogMe.log(sum.toString())
        UIUtils.setUIElementValue(this, containerView.findViewById(R.id.activity_get_order_estimates__total_kg), "$sum kg")
    }

    private fun updateTotalPc() {
        LogMe.log("a")
        var sum = 0
        uiEntriesList.forEach {
            LogMe.log("b")
            val pc = "0${UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc))}"
            LogMe.log("c: $pc")
            sum += pc.toInt()
            LogMe.log("d: $sum")
        }
        LogMe.log("e: $sum.toString()")
        UIUtils.setUIElementValue(this, containerView.findViewById(R.id.activity_get_order_estimates__total_pc), "$sum pc")
    }

    private fun getCustomerNamesAsStringList(): List<String> {
        val namesList = mutableListOf<String>()

        CustomerKYC.getAllCustomers().forEach {
            namesList.add(it.getDisplayName())
        }
        return namesList
    }

//    @SuppressLint("ClickableViewAccessibility")
//    fun populateCustomerList() {
//        val dropDown = findViewById<MaterialAutoCompleteTextView>(R.id.activity_get_order_estimates__customer_selection_dropdown)
//        val adapter: ArrayAdapter<String> =
//            ArrayAdapter<String>(AppContexts.get(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, getCustomerNamesAsStringList())
//        (dropDown as MaterialAutoCompleteTextView).setAdapter(adapter)
//
//        dropDown.setOnTouchListener { _, _ ->
//                dropDown.showDropDown()
//                dropDown.requestFocus()
//                false
//            }
//
//        dropDown.setOnItemClickListener { parent, arg1, position, arg3 ->
//            val item = parent.getItemAtPosition(position)
//            LogMe.log("Selected Customer: $item")
//            if(!isOnList(item.toString())) {
//                createEstimatesView(item.toString())
//            } else {
//                Toast.makeText(this, "Already on list", Toast.LENGTH_SHORT).show()
//            }
//            dropDown.text.clear()
//        }
//    }

    fun createNGetObjectsFromUI(): List<GetCustomerOrders> {
        val list = mutableListOf<GetCustomerOrders>()
        uiEntriesList.forEach {
            val seqNo = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_sl_no))
            val name = UIUtils.getUIElementValue(it.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name))
            val pc = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc))
            val kg = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg))
            val rate = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_rate))
            val obj = GetCustomerOrders(id = System.currentTimeMillis().toString(),
                name = name,
                seqNo = seqNo,
                estimatePc = pc,
                estimateKg = kg,
                rate = rate,
                prevDue = "0")
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

    fun onClickSaveBtn(view: View) {
        Toast.makeText(this, "Saving Data", Toast.LENGTH_SHORT).show()
        GetCustomerOrders.deleteAll()
        GetCustomerOrders.save(createNGetObjectsFromUI())

        val metadataObj = SingleAttributedData.getRecords()
        metadataObj.estimatedLoadPc = UIUtils.getUIElementValue(LoadModel.getUiElementFromOrderingPage(view, LoadModel::requiredPc))
        metadataObj.estimatedLoadKg = UIUtils.getUIElementValue(LoadModel.getUiElementFromOrderingPage(view, LoadModel::requiredKg))

        SingleAttributedData.save(metadataObj)
        Toast.makeText(this, "Data save complete!", Toast.LENGTH_LONG).show()
    }
}