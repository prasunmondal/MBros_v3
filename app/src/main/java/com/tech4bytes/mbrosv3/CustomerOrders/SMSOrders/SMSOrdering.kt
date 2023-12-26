package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.SMSPermissions
import com.tech4bytes.mbrosv3.Sms.SmsReader
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ListUtils
import com.tech4bytes.mbrosv3.Utils.T4B.StringUtils
import org.apache.commons.collections4.CollectionUtils
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import java.util.stream.Collectors


class SMSOrdering : AppCompatActivity() {

    var orders = mutableListOf<SMSOrderModel>()
    var smsToProcess: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smsordering)
        AppContexts.set(this)
        AppUtils.logError()
        supportActionBar!!.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        SMSPermissions.askPermission(this, this)
        SMSPermissions.askPermission(this, this)

        Thread {
            setUpUI()
            populateCustomerListDropdown()
            setUpListeners()
            showSMS()
        }.start()
    }

    fun setUpUI() {
        val offset = 1000L * 60 * 60 * 12;
        val currentDate = System.currentTimeMillis()
        val listDate = Date(currentDate + offset)

        runOnUiThread {
            val listDateUI = findViewById<TextView>(R.id.smsordering_list_date)
            listDateUI.text = DateUtils.getDateInFormat(listDate, "dd/MM/yyyy")
        }
    }

    private fun setUpListeners() {
        findViewById<EditText>(R.id.smsorder_avg_wt1).doOnTextChanged { text, start, before, count ->
            processSMS()
            showEntries()
            updateTotal()
        }
    }

    @SuppressLint("SetTextI18n")
    fun showSMS() {
            val smsFiltered = SmsReader.getAllSms(this, this, StringUtils.getListFromCSV(AppConstants.get(AppConstants.SMS_ORDER_GET_ORDER_PH_NUMBER)).toTypedArray())
            val container = findViewById<LinearLayout>(R.id.smsorders_sms_view_container)

            runOnUiThread {
                findViewById<TextView>(R.id.smsordering_loading_sms_label).visibility = View.GONE
            }

            smsFiltered.forEach { sms ->
                runOnUiThread {
                    val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_fragments, null)
                    entry.findViewById<TextView>(R.id.smsorder_listEntry_receive_number).text = sms.number
                    entry.findViewById<TextView>(R.id.smsorder_listEntry_text).text = sms.body
                    entry.findViewById<TextView>(R.id.smsorder_listEntry_date).text = sms.datetime.split(" ")[2]
                    entry.findViewById<TextView>(R.id.smsorder_listEntry_month).text = sms.datetime.split(" ")[1]
                    container.addView(entry)
                    entry.setOnClickListener {
                        smsToProcess = sms.body
                        processSMS()
                        showEntries()
                        onClickToggleSMSView(entry)
                    }
                }
            }
    }

    private fun processSMS() {
        val valueStr = smsToProcess
        val valueArray = valueStr.split("+")
        val namesArray = AppConstants.get(AppConstants.SMS_ORDER_SEQUENCE).split(",")
        val minSize = Math.min(valueArray.size, namesArray.size)

        var totalKg = 0
        var totalPc = 0
        orders = mutableListOf()
        for (j in 0 until minSize) {
            if (NumberUtils.getIntOrZero(valueArray[j].trim()) != 0) {
                totalKg += NumberUtils.getIntOrZero(valueArray[j].trim())
                var avgWt1 = getAvgWt1()
                if (avgWt1 == 0.0) {
                    avgWt1 = 1.0
                }
                val calculatedPc = NumberUtils.getIntOrZero(valueArray[j].trim()) / avgWt1
                var df = DecimalFormat("#.#")
                val calculatedPcDouble = df.format(calculatedPc).toDouble()

                df = DecimalFormat("#")
                df.roundingMode = RoundingMode.CEILING
                val finalizedPc1Double = df.format(calculatedPcDouble).toDouble()
                val finalizedPc1 = finalizedPc1Double.toInt()
                totalPc += finalizedPc1

                orders.add(SMSOrderModel(System.currentTimeMillis().toString(), namesArray[j].trim(), valueArray[j].trim().toInt(), calculatedPcDouble, finalizedPc1))
            }
        }
        populateCustomerListDropdown()
    }

    private fun addCustomer(name: String) {
        if (orders.none { it.name == name }) {
            // if the name is not already present in the list
            orders.add(SMSOrderModel(System.currentTimeMillis().toString(), name, NumberUtils.getIntOrZero("0"), NumberUtils.getDoubleOrZero("0"), NumberUtils.getIntOrZero("0")))
            showEntries()
        }
    }

    private fun populateCustomerListDropdown() {
        val sortedList = ListUtils.getAllPossibleValuesList(CustomerKYC.getAllCustomers(), CustomerKYCModel::nameEng).toList()

        // remove already showing names from dropdown
        val alreadyInUI: List<String> = orders.stream()
            .map(SMSOrderModel::name)
            .collect(Collectors.toList())
        val listToShow = CollectionUtils.subtract(sortedList, alreadyInUI).toList()
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.template_dropdown_entry, listToShow)

        runOnUiThread {
            val uiView = findViewById<AutoCompleteTextView>(R.id.smsorder_customer_picker)
            uiView.setAdapter(adapter)
            uiView.threshold = 0
            uiView.setText("")
            uiView.setOnTouchListener { _, _ ->
                uiView.showDropDown()
                uiView.requestFocus()
                false
            }
            uiView.setOnItemClickListener { adapterView, view, i, l ->
                addCustomer(uiView.text.toString())
                showEntries()
                populateCustomerListDropdown()
                uiView.setText("")
                uiView.hint = "+Customer"
            }
        }
    }

    private fun showEntries() {
        val orderListContainer = findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        orderListContainer.removeAllViews()
        orders = Sorter.sortByNameList(orders, SMSOrderModel::name) as MutableList<SMSOrderModel>
        for (j in 0 until orders.size) {
            runOnUiThread {
                val balance = CustomerDueData.getBalance(orders[j].name)
                val entry = layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
                entry.findViewById<TextView>(R.id.smsorder_listEntry_calculated_pc).text = orders[j].calculatedPc.toString()

                val finalizedPcView = entry.findViewById<EditText>(R.id.smsorder_listEntry_pc)
                finalizedPcView.hint = orders[j].orderedPc.toString()
                finalizedPcView.doOnTextChanged { text, start, before, count ->
                    orders[j].orderedPc = NumberUtils.getIntOrZero(UIUtils.getTextOrHint(finalizedPcView))
                    updateTotal()
                }

                entry.findViewById<TextView>(R.id.smsorder_listEntry_date).text = orders[j].orderedKg.toString()
                entry.findViewById<TextView>(R.id.smsorder_listEntry_number).text = orders[j].name
                if(CustomerKYC.getCustomerByEngName(orders[j].name)!!.customerAccount.trim().isNotEmpty() && orders[j].name != CustomerKYC.getCustomerByEngName(orders[j].name)!!.customerAccount) {
                    entry.findViewById<TextView>(R.id.smsorder_listEntry_amount).text = "+ ${CustomerKYC.getCustomerByEngName(orders[j].name)!!.customerAccount}"
                } else {
                    entry.findViewById<TextView>(R.id.smsorder_listEntry_amount).text = "$balance"
                }
                orderListContainer.addView(entry)
            }
        }
        showTotal()
    }

    var totalEntryView: View? = null
    fun showTotal() {
        val orderListContainer = findViewById<LinearLayout>(R.id.smsorders_order_list_view_container)
        totalEntryView = layoutInflater.inflate(R.layout.activity_sms_ordering_list_fragments, null)
        updateTotal()
        orderListContainer.addView(totalEntryView)
    }

    fun updateTotal() {
        var totalKg = 0
        var totalPc = 0
        for (j in 0 until orders.size) {
            totalPc += orders[j].orderedPc
            totalKg += orders[j].orderedKg
        }

        val totalPcsField = totalEntryView?.findViewById<EditText>(R.id.smsorder_listEntry_pc)
        totalPcsField?.setText(totalPc.toString())
        totalPcsField?.setTextColor(ContextCompat.getColor(this, androidx.appcompat.R.color.material_blue_grey_800))
        totalPcsField?.setTypeface(null, Typeface.BOLD)

        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_date)?.text = "$totalKg"
        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_number)?.text = "TOTAL"
        totalEntryView?.findViewById<TextView>(R.id.smsorder_listEntry_amount)?.text = ""
    }

    fun getAvgWt1(): Double {
        return NumberUtils.getDoubleOrZero(findViewById<EditText>(R.id.smsorder_avg_wt1).text.toString())
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

            SMSOrderModel.deleteAllDataInServer()
            var count = 1
            orders.forEach {
                runOnUiThread {
                    saveBtn.text = "Saving (${count++}/${orders.size})"
                }
                SMSOrderModel.save(it)
            }

            runOnUiThread {
                saveBtn.isEnabled = true
                saveBtn.alpha = 1.0f
                saveBtn.isClickable = true
                saveBtn.text = "Save"
            }
        }.start()
    }

    fun onClickToggleSMSView(view: View) {
        val c = findViewById<ScrollView>(R.id.smsorders_sms_view_scroll_container)
        val b = findViewById<TextView>(R.id.smsordering_toggle_sms_text)
        c.visibility = if (c.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        b.text = if (c.visibility == View.VISIBLE) "HIDE SMS" else "SHOW SMS"
    }
}