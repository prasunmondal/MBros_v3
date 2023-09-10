package com.tech4bytes.mbrosv3.Customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.time.LocalDateTime
import kotlin.streams.toList

class DueShow : AppCompatActivity() {

    lateinit var toggleBalanceViewBtn: Switch
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_due_show)
        AppContexts.set(this, this)

        setUI()
        showDues(toggleBalanceViewBtn.isChecked)
    }

    private fun isRecordInTimeRange(data: CustomerData, startingTime: LocalDateTime, endingTime: LocalDateTime): Boolean {
        val t = LocalDateTime.parse(data.timestamp.replace("Z",""))
        return t!!.isAfter(startingTime) && t.isBefore(endingTime)
    }

    fun getAvgDue(name: String, daysBack: Int, daysToAvg: Int): Int {
        val currentTimestamp = LocalDateTime.now()
        val startingTime = currentTimestamp.minusDays((daysBack + daysToAvg/2).toLong())
        val endingTime = currentTimestamp.minusDays((daysBack - daysToAvg/2).toLong())
        
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

//        var sum = 0
//        var count = 0
//        LogMe.log("Name: $name")
//        list = list.sortedBy { p -> LocalDateTime.parse(p.timestamp.replace("Z","")) }.reversed()
//        list.forEach { data ->
//            val t = LocalDateTime.parse(data.timestamp.replace("Z",""))
//            if (t!!.isAfter(startingTime) && t.isBefore(endingTime)) {
//                LogMe.log("$count. Date: ${data.timestamp}, Amount Due ${data.balanceDue}")
//                sum += data.balanceDue.toInt()
//                count++
//            }
//            else if(t.isBefore(startingTime)) {
//                return if(count == 0) {
//                    LogMe.log("Returning outOfTime Record")
//                    LogMe.log("Name: ${data.name}, Date: ${data.timestamp}, Amount Due: ${data.balanceDue}")
//                    data.balanceDue.toInt()
//                } else
//                    sum / count
//            }
//        }
//        LogMe.log(" --- TOTAL: $sum")
//        LogMe.log(" --- AVG: ${if(count == 0) 0 else sum / count}")
//        return if(count == 0) 0 else sum / count
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

    private fun showDues(showAfterDeliveryBalance: Boolean = true) {
        val listContainer = findViewById<LinearLayout>(R.id.activity_due_show_fragment_conntainer)
        listContainer.removeAllViews()
        var latestRecords = removeInActiveCustomers(CustomerData.getAllLatestRecords())
        latestRecords = sortByNameList(latestRecords, CustomerKYC.getAllCustomers())

        val balanceTextColor = if (showAfterDeliveryBalance) R.color.due_show_including_finalized_transactions else R.color.due_show_excluding_finalized_transactions
        val latestBalanceAfterDelivery = CustomerDueData.getBalance(showAfterDeliveryBalance)

        latestRecords.forEach {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_due_show_entry, null)
            val nameElement = entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role)
            val amountElement = entry.findViewById<TextView>(R.id.activity_due_show_amount)
            val dueChangeElement = entry.findViewById<TextView>(R.id.activity_due_show_change_in_duration1)

            val currentBalance = latestBalanceAfterDelivery[it.name]
            val balanceB4X1Days = getAvgDue(it.name, 30, AppConstants.get(AppConstants.DUE_SHOW_BALANCE_AVG_DAYS).toInt())
            val changeInDuration1 = currentBalance!! - balanceB4X1Days
            LogMe.log("Name: ${it.name}, currentDue: $currentBalance, balanceB4XDays: $balanceB4X1Days, Change: $changeInDuration1")
            nameElement.text = it.name
            amountElement.text = currentBalance.toString()
            dueChangeElement.text = changeInDuration1.toString()
            val balanceDiffTextColor = if (changeInDuration1 > 0) R.color.due_show_balance_increased else R.color.due_show_balance_decreased
            amountElement.setTextColor(ContextCompat.getColor(this, balanceTextColor))
            dueChangeElement.setTextColor(ContextCompat.getColor(this, balanceDiffTextColor))

            listContainer.addView(entry)
        }
    }

    private fun sortByNameList(list: MutableList<CustomerData>, sortedList: List<CustomerKYCModel>): MutableList<CustomerData> {
        val map: MutableMap<Int, CustomerData> = mutableMapOf()
        val listOfEntriesNotPresentInCustomerKYC: MutableList<CustomerData> = mutableListOf()
        list.forEach { toSortItem ->
            listOfEntriesNotPresentInCustomerKYC.add(toSortItem)
            var index = 0
            sortedList.forEach { sortedItem ->
                if (sortedItem.nameEng == toSortItem.name) {
                    map[index] = toSortItem
                    listOfEntriesNotPresentInCustomerKYC.remove(toSortItem)
                }
                index++
            }
        }
        val sorted = ArrayList(map.toSortedMap().values)
        sorted.addAll(listOfEntriesNotPresentInCustomerKYC)
        return sorted
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}