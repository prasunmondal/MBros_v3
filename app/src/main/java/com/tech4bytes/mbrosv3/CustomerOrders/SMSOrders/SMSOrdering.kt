package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.CustomerOrders.Occasions.EventsUI
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.SmsReader
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.Utils.T4B.StringUtils
import java.util.Date
import java.util.stream.Collectors
import kotlin.math.ceil


class SMSOrdering : AppCompatActivity() {

    var orders = mutableListOf<SMSOrderModel>()
    var smsToProcess: String = ""
    var listViews: MutableMap<String, View> = mutableMapOf()
    lateinit var latestBalances: MutableMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smsordering)
        AppContexts.set(this)
        AppUtils.logError(this)
        supportActionBar!!.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Thread {
            EventsUI.showEvents(this, findViewById(R.id.smsordering_events_layout))
        }.start()

        Thread {
            latestBalances = CustomerDueData.getBalance()
            setUpUI()
            fetchFromServer()
            populateCustomerListDropdown()
            setUpListeners()
            showSMS()
            showEntries()
            showTotal()
            markUIReadyToUse()
        }.start()
    }

    private fun markUIReadyToUse() {
        runOnUiThread {
            val labelsContainer = findViewById<LinearLayout>(R.id.smso_wait_labels_container)
            labelsContainer.visibility = View.GONE
        }
    }

    fun setUpUI() {
        val offset = 1000L * 60 * 60 * 12
        val currentDate = System.currentTimeMillis()
        val listDate = Date(currentDate + offset)

        runOnUiThread {
            val listDateUI = findViewById<TextView>(R.id.smsordering_list_date)
            listDateUI.text = DateUtils.getDateInFormat(listDate, "dd/MM/yyyy")
        }
    }

    private fun setUpListeners() {
        findViewById<EditText>(R.id.smsorder_avg_wt1).doOnTextChanged { text, start, before, count ->
            refreshEntries()
        }
        findViewById<EditText>(R.id.smsorder_avg_wt2).doOnTextChanged { text, start, before, count ->
            refreshEntries()
        }
    }

    @SuppressLint("SetTextI18n")
    fun showSMS() {
        val smsFiltered = SmsReader.getAllSms(
            this,
            this,
            StringUtils.getListFromCSV(AppConstants.get(AppConstants.SMS_ORDER_GET_ORDER_PH_NUMBER))
                .toTypedArray()
        )
        val container = findViewById<LinearLayout>(R.id.smsorders_sms_view_container)

        runOnUiThread {
            findViewById<TextView>(R.id.smsordering_loading_sms_label).visibility = View.GONE
        }

        smsFiltered.forEach { sms ->
            runOnUiThread {
                val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_fragments, null)
                entry.findViewById<TextView>(R.id.smsorder_listEntry_receive_number).text =
                    sms.number
                entry.findViewById<TextView>(R.id.smsorder_listEntry_text).text = sms.body
                entry.findViewById<TextView>(R.id.smsorder_listEntry_date).text =
                    sms.datetime.split(" ")[2]
                entry.findViewById<TextView>(R.id.smsorder_listEntry_month).text =
                    sms.datetime.split(" ")[1]
                container.addView(entry)
                entry.setOnClickListener {
                    smsToProcess = sms.body
                    processSMS()
                    showEntries(true)
                    onClickToggleSMSView(entry)
                }
            }
        }
    }

    private fun processSMS() {
        val valueStr = smsToProcess
//        val valueStr = "3+5+7+8+9+9+6+7+8+8+7"
        val valueArray = valueStr.split("+")
        val namesArray = AppConstants.get(AppConstants.SMS_ORDER_SEQUENCE).split(",")
        val minSize = Math.min(valueArray.size, namesArray.size)

        val orderListContainer =
            findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        orderListContainer.removeAllViews()
        orders = mutableListOf()
        for (j in 0 until minSize) {
            if (NumberUtils.getIntOrZero(valueArray[j].trim()) != 0) {
                var avgWt1 = getAvgWt1()
                if (avgWt1 == 0.0) {
                    avgWt1 = 1.0
                }
                val finalPc = (NumberUtils.getIntOrZero(valueArray[j].trim()) / avgWt1).toInt()
                orders.add(
                    SMSOrderModel(
                        System.currentTimeMillis().toString(),
                        namesArray[j].trim(),
                        valueArray[j].trim().toInt(),
                        "",
                        finalPc, finalPc, SMSOrderModelUtil.getAvgWt1(), SMSOrderModelUtil.getAvgWt2()
                    )
                )
            }
        }
        populateCustomerListDropdown()
    }

    private fun fetchFromServer() {
        orders = SMSOrderModelUtil.fetchAll().execute() as MutableList<SMSOrderModel>
        runOnUiThread {
            findViewById<EditText>(R.id.smsorder_avg_wt1).setText(SMSOrderModelUtil.getAvgWt1())
            findViewById<EditText>(R.id.smsorder_avg_wt2).setText(SMSOrderModelUtil.getAvgWt2())
        }
        populateCustomerListDropdown()
    }

    private fun addCustomer(name: String) {
        if (orders.none { it.name == name }) {
            // if the name is not already present in the list
            orders.add(SMSOrderModel(System.currentTimeMillis().toString(), name, 0, "", 0, 0, SMSOrderModelUtil.getAvgWt1(), SMSOrderModelUtil.getAvgWt2()))
            showEntries(true)
        }
    }

    private fun populateCustomerListDropdown() {
        val activeCustomers = CustomerKYC.fetchAll().execute().filter { it.isActiveCustomer.toBoolean() }
        val inActiveCustomers = CustomerKYC.fetchAll().execute().filter { !it.isActiveCustomer.toBoolean() }

        val sortedActiveList = ListUtils.getAllPossibleValuesList(activeCustomers, CustomerKYCModel::nameEng).toList()
        val sortedInActiveList = ListUtils.getAllPossibleValuesList(inActiveCustomers, CustomerKYCModel::nameEng).toList()

        runOnUiThread {
            val uiView = findViewById<AutoCompleteTextView>(R.id.smsorder_customer_picker)
            uiView.setAdapter(getListAdapter(sortedActiveList, alreadySelectedCustomers()))
            uiView.threshold = 0
            uiView.setText("")
            uiView.setOnTouchListener { _, _ ->
                uiView.showDropDown()
                uiView.requestFocus()
                false
            }
            uiView.setOnItemClickListener { _, _, _, _ ->
                addCustomer(uiView.text.toString().trim())
                uiView.setText("")
                uiView.setAdapter(getListAdapter(sortedActiveList, alreadySelectedCustomers()))
                uiView.post {
                    uiView.showDropDown()
                }
            }
        }

        runOnUiThread {
            val uiView = findViewById<AutoCompleteTextView>(R.id.smsorder_inactive_customers_picker)
            uiView.setAdapter(getListAdapter(sortedInActiveList, alreadySelectedCustomers()))
            uiView.threshold = 0
            uiView.setText("")
            uiView.setOnTouchListener { _, _ ->
                uiView.showDropDown()
                uiView.requestFocus()
                false
            }
            uiView.setOnItemClickListener { _, _, _, _ ->
                addCustomer(uiView.text.toString().trim())
                uiView.setText("")
                uiView.setAdapter(getListAdapter(sortedInActiveList, alreadySelectedCustomers()))
                uiView.post {
                    uiView.showDropDown()
                }
            }
        }
    }

    private fun getListAdapter(masterList: List<String>, toBeDuducted: List<String>): ArrayAdapter<String> {
        val remainingSuggestions = masterList.filterNot { it in toBeDuducted }
        return ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, remainingSuggestions)
    }
    private fun alreadySelectedCustomers(): List<String> {
        return orders.stream().map(SMSOrderModel::name).collect(Collectors.toList()).toList()
    }

    private fun showEntries(clearPreviousEntries: Boolean = false) {
        runOnUiThread {
            val orderListContainer =
                findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
            if (clearPreviousEntries)
                orderListContainer.removeAllViews()
            orders =
                Sorter.sortByNameList(orders, SMSOrderModel::name) as MutableList<SMSOrderModel>
            orders.forEach { order ->
                val balance = latestBalances[order.name]
                val entry =
                    layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
                val finalizedPcView = entry.findViewById<EditText>(R.id.smsorder_listEntry_pc)
                val finalizedKgView = entry.findViewById<EditText>(R.id.smsorder_list_finalized_kg)
                val removeBtn = entry.findViewById<ImageView>(R.id.smsorder_listEntry_remove)

                finalizedPcView.setText(order.appPc)
                if (order.orderedKg > 0)
                    finalizedKgView.setText(order.orderedKg.toString())
                refreshHints(entry, order)

                finalizedPcView.doOnTextChanged { text, start, before, count ->
                    refreshHints(entry, order)
                    order.finalPc = NumberUtils.getIntOrZero(UIUtils.getTextOrHint(finalizedPcView))
                    order.appPc =
                        if (NumberUtils.getIntOrZero(finalizedPcView.text.toString()) == 0) "" else finalizedPcView.text.toString()
                    updateTotal()
                }

                finalizedKgView.doOnTextChanged { text, start, before, count ->
                    refreshHints(entry, order)
                    order.orderedKg =
                        NumberUtils.getIntOrZero(UIUtils.getTextOrHint(finalizedKgView))
                    order.finalPc = NumberUtils.getIntOrZero(UIUtils.getTextOrHint(finalizedPcView))
                    updateTotal()
                }

                removeBtn.setOnClickListener {
                    confirmOrderDeletion(orderListContainer, order, entry)
                }

                entry.findViewById<TextView>(R.id.smsorder_listEntry_number).text = order.name
                entry.findViewById<TextView>(R.id.smsorder_listEntry_amount).text = "$balance"
                orderListContainer.addView(entry)
                listViews[order.name] = entry
            }
            showTotal()
        }
    }

    var totalEntryView: View? = null
    fun showTotal() {
        runOnUiThread {
            val orderListContainer =
                findViewById<LinearLayout>(R.id.smsorders_order_extra_aand_total_view_container)
            orderListContainer.removeAllViews()
            totalEntryView =
                layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
            orderListContainer.addView(totalEntryView)
            updateTotal()
        }
    }

    fun onClickDeleteAllBtn(view: View) {
        val orderListContainer =
            findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        confirmDeleteAllOrder(orderListContainer)
    }

    private fun confirmOrderDeletion(orderListContainer: LinearLayout, order: SMSOrderModel, entry: View) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Deleting order for: " + order.name)
            .setTitle("Delete Order?")
            .setPositiveButton("Confirm") { dialog, id ->
                // CONFIRM
                orders.remove(order)
                listViews.remove(order.name)
                orderListContainer.removeView(entry)
                refreshHints(entry, order)
                updateTotal()
                populateCustomerListDropdown()
            }
            .setNegativeButton("Cancel") { dialog, id ->
                // CANCEL
            }.setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun confirmDeleteAllOrder(orderListContainer: LinearLayout) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Deleting all orders")
            .setTitle("Delete Orders?")
            .setPositiveButton("Confirm") { dialog, id ->
                // CONFIRM
                orders = mutableListOf()
                listViews.clear()
                orderListContainer.removeAllViews()
                updateTotal()
                populateCustomerListDropdown()
            }
            .setNegativeButton("Cancel") { dialog, id ->
                // CANCEL
            }.setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun updateTotal() {
        var totalKg = 0
        var totalPc = 0
        for (j in 0 until orders.size) {
            totalPc += orders[j].finalPc
            totalKg += orders[j].orderedKg
        }

        val totalPcsField = totalEntryView?.findViewById<EditText>(R.id.smsorder_listEntry_pc)
        val totalKgsField = totalEntryView?.findViewById<EditText>(R.id.smsorder_list_finalized_kg)

        totalPcsField?.setText(totalPc.toString())
        totalPcsField?.setTextColor(
            ContextCompat.getColor(
                this,
                androidx.appcompat.R.color.material_blue_grey_800
            )
        )
        totalPcsField?.setTypeface(null, Typeface.BOLD)

        totalKgsField?.setText(totalKg.toString())
        totalKgsField?.setTextColor(
            ContextCompat.getColor(
                this,
                androidx.appcompat.R.color.material_blue_grey_800
            )
        )
        totalKgsField?.setTypeface(null, Typeface.BOLD)

        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_number)?.text = "TOTAL"
        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_amount)?.text = ""
    }

    fun refreshHints(entry: View, order: SMSOrderModel?) {
        val estimatedPcsHintView1 =
            entry.findViewById<TextView>(R.id.smsorder_listEntry_calculated_pc)
        val estimatedKgsHintView1 = entry.findViewById<TextView>(R.id.smsorder_listEntry_approx_kg)
        val estimatedPcsTextView = entry.findViewById<TextView>(R.id.smsorder_listEntry_pc)
        if (getAvgWt1() > 0.0) {
            val estimatedPc1: Double =
                NumberUtils.getDoubleOrZero((getEntryKg(entry) / getAvgWt1()).toString())
            val estimatedkg1: Double =
                NumberUtils.getDoubleOrZero((getAvgWt1() * getPc(entry)).toString())
            estimatedPcsHintView1.text = String.format("%.1f", estimatedPc1)
            estimatedKgsHintView1.text = String.format("%.1f", estimatedkg1)
            estimatedPcsTextView.hint = String.format(ceil(estimatedPc1).toInt().toString())
        } else {
            estimatedPcsHintView1.text = ""
            estimatedKgsHintView1.text = ""
            estimatedPcsTextView.hint = "0"
        }

        val estimatedPcsHintView2 = entry.findViewById<TextView>(R.id.smsorder_listEntry_approx_pc2)
        val estimatedKgsHintView2 = entry.findViewById<TextView>(R.id.smsorder_listEntry_approx_kg2)
        if (getAvgWt2() > 0.0) {
            val estimatedPc2: Double =
                NumberUtils.getDoubleOrZero((getEntryKg(entry) / getAvgWt2()).toString())
            val estimatedkg2: Double =
                NumberUtils.getDoubleOrZero((getAvgWt2() * getPc(entry)).toString())
            estimatedPcsHintView2.text = String.format("%.1f", estimatedPc2)
            estimatedKgsHintView2.text = String.format("%.1f", estimatedkg2)
        } else {
            estimatedPcsHintView2.text = ""
            estimatedKgsHintView2.text = ""
        }
    }

    fun getAvgWt1(): Double {
        return NumberUtils.getDoubleOrZero(findViewById<EditText>(R.id.smsorder_avg_wt1).text.toString())
    }

    fun getAvgWt2(): Double {
        return NumberUtils.getDoubleOrZero(findViewById<EditText>(R.id.smsorder_avg_wt2).text.toString())
    }

    fun getPc(entry: View): Int {
        return NumberUtils.getIntOrZero(UIUtils.getTextOrHint(entry.findViewById(R.id.smsorder_listEntry_pc)))
    }

    fun getEntryKg(entry: View): Int {
        return NumberUtils.getIntOrZero(UIUtils.getTextOrHint(entry.findViewById(R.id.smsorder_list_finalized_kg)))
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }

    fun onClickSaveSMSOrdersBtn(view: View) {
        val saveBtn = view as Button

        Thread {
            runOnUiThread {
                saveBtn.isEnabled = false
                saveBtn.alpha = .5f
                saveBtn.isClickable = false
                saveBtn.text = "Deleting previous data"
            }

            runOnUiThread {
                saveBtn.text = "Saving ${orders.size} reecords)"
            }

            orders.forEach {
                it.orderedPc = it.finalPc
                it.avgWt1 = findViewById<EditText>(R.id.smsorder_avg_wt1).text.toString()
                it.avgWt2 = findViewById<EditText>(R.id.smsorder_avg_wt2).text.toString()
            }
            SMSOrderModelUtil.save(orders).execute()

            runOnUiThread {
                saveBtn.isEnabled = true
                saveBtn.alpha = 1.0f
                saveBtn.isClickable = true
                saveBtn.text = "Save"
            }
        }.start()
    }

    fun refreshEntries() {
        listViews.forEach {
            val orderObj = orders.stream()
                .filter { person -> person.name.equals(it.key) }
                .findFirst().get()
            refreshHints(it.value, orderObj)
        }
        updateTotal()
    }

    fun onClickToggleSMSView(view: View) {
        val c = findViewById<ScrollView>(R.id.smsorders_sms_view_scroll_container)
        val b = findViewById<TextView>(R.id.smsordering_toggle_sms_text)
        c.visibility = if (c.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        b.text = if (c.visibility == View.VISIBLE) "HIDE SMS" else "SHOW SMS"
    }
}