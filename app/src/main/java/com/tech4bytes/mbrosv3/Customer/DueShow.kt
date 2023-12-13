package com.tech4bytes.mbrosv3.Customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessLogic.Sorter
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.time.LocalDateTime

class DueShow : AppCompatActivity() {

    lateinit var toggleBalanceViewBtn: Switch
    var balanceMapAfterDelivery: MutableMap<String, Int>? = null
    var balanceMapBeforeDelivery: MutableMap<String, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_due_show)
        AppContexts.set(this, this)

        setUI()
        showDues(toggleBalanceViewBtn.isChecked)
    }

    private fun getBalanceMap(showAfterDeliveryBalance: Boolean): MutableMap<String, Int> {
        return if (showAfterDeliveryBalance) {
            if (balanceMapAfterDelivery == null)
                balanceMapAfterDelivery = CustomerDueData.getBalance()
            balanceMapAfterDelivery as MutableMap<String, Int>
        } else {
            if (balanceMapBeforeDelivery == null)
                balanceMapBeforeDelivery = CustomerDueData.getBalance(shouldIncludePostDeliveryUpdates = false, includeStagedPayments = false)
            balanceMapBeforeDelivery as MutableMap<String, Int>
        }
    }

    private fun isRecordInTimeRange(data: CustomerData, startingTime: LocalDateTime, endingTime: LocalDateTime): Boolean {
        val t = LocalDateTime.parse(data.timestamp.replace("Z", ""))
        return t!!.isAfter(startingTime) && t.isBefore(endingTime)
    }

    fun getAvgDue(name: String, daysBack: Int, daysToAvg: Int): Int {
        val currentTimestamp = LocalDateTime.now()
        val startingTime = currentTimestamp.minusDays((daysBack + daysToAvg / 2).toLong())
        val endingTime = currentTimestamp.minusDays((daysBack - daysToAvg / 2).toLong())

        // <--------------------|------------------------------|-------------------->
        // 2000            startingTime                    endingTime              Now
        //                                                        <------- list starts

        LogMe.log("Name :$name")
        val listSorted = CustomerData.getRecords()
            .filter { it.name == name }
            .sortedBy { t -> t.timestamp }.reversed()
        try {
            // return the avg of the records found within the time range
            return listSorted
                .filter { t -> isRecordInTimeRange(t, startingTime, endingTime) }
                .stream().mapToInt { i -> i.balanceDue.toInt() }
                .average().asDouble.toInt()
        } catch (e: NoSuchElementException) {
            return try {
                // No records found within the time range. Returning last data found earlier to the time range
                listSorted
                    .filter { t -> LocalDateTime.parse(t.timestamp.replace("Z", "")).isBefore(startingTime) }
                    .stream().findFirst().get().balanceDue.toInt()
            } catch (e: NoSuchElementException) {
                // No records found within the time range and earlier to it. Returning zero
                0
            }
        }
    }

    private fun setUI() {
        toggleBalanceViewBtn = findViewById(R.id.toggleBalanceDueView)
        toggleBalanceViewBtn.setOnCheckedChangeListener { _, isChecked ->
            showDues(isChecked)
        }
    }

    fun shouldShow(customerData: CustomerData): Boolean {
        if (NumberUtils.getIntOrZero(customerData.balanceDue) != 0)
            return true
        if (CustomerKYC.getCustomerByEngName(customerData.name) == null)
            return false
        if (CustomerKYC.getCustomerByEngName(customerData.name)!!.isActiveCustomer.toBoolean())
            return true
        return false
    }

    private fun removeInActiveCustomers(list: MutableList<CustomerData>): MutableList<CustomerData> {
        val filteredList = list.stream().filter { p -> shouldShow(p) }.toList()
        return filteredList.toMutableList()
    }

    private fun showDeltas(name: String, currentBalance: Int, view: TextView) {
        val noOfDaysBack = NumberUtils.getIntOrZero(findViewById<TextView>(R.id.noOfDaysBack).text.toString())
        Thread {
            val balanceB4X1Days = getAvgDue(name, noOfDaysBack, AppConstants.get(AppConstants.DUE_SHOW_BALANCE_AVG_DAYS).toInt())
            val changeInDuration1 = currentBalance - balanceB4X1Days
            val balanceDiffTextColor = if (changeInDuration1 > 0) R.color.due_show_balance_increased else R.color.due_show_balance_decreased
            runOnUiThread {
                view.text = changeInDuration1.toString()
                view.setTextColor(ContextCompat.getColor(this, balanceDiffTextColor))
            }
        }.start()
    }

    private fun showDues(showAfterDeliveryBalance: Boolean = true) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_due_show_fragment_conntainer)
        listContainer.removeAllViews()
        var latestRecords = removeInActiveCustomers(CustomerData.getAllLatestRecords())
        latestRecords = Sorter.sortByNameList(latestRecords, CustomerData::name) as MutableList<CustomerData>

        val balanceTextColor = if (showAfterDeliveryBalance) R.color.due_show_including_finalized_transactions else R.color.due_show_excluding_finalized_transactions

        latestRecords.forEach {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_due_show_entry, null)
            val nameElement = entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role)
            val amountElement = entry.findViewById<TextView>(R.id.activity_due_show_amount)
            val dueChangeElement = entry.findViewById<TextView>(R.id.activity_due_show_change_in_duration1)

            val currentBalance = getBalanceMap(showAfterDeliveryBalance)[it.name]
            nameElement.text = it.name
            amountElement.text = currentBalance.toString()
            amountElement.setTextColor(ContextCompat.getColor(this, balanceTextColor))
            showDeltas(it.name, currentBalance!!, dueChangeElement)

            listContainer.addView(entry)
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }

    fun onChangeDaysOffsetToCompareBalance(view: View) {
        showDues()
    }
}