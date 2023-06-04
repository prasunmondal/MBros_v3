package com.tech4bytes.mbrosv3.Customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import kotlin.streams.toList

class DueShow : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_due_show)
        AppContexts.set(this, this)

        showDues()
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

    fun removeInActiveCustomers(list: MutableList<CustomerData>): MutableList<CustomerData> {
        val filteredList = list.stream().filter { p -> shouldShow(p) }.toList()
        return filteredList.toMutableList()
    }

    fun showDues() {
        val listContainer = findViewById<LinearLayout>(R.id.activity_due_show_fragment_conntainer)
        var latestRecords = removeInActiveCustomers(CustomerData.getAllLatestRecords())
        latestRecords = sortByNameList(latestRecords, CustomerKYC.getAllCustomers())
        latestRecords.forEach {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_due_show_entry, null)

            var nameElement = entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role)
            var amountElement = entry.findViewById<TextView>(R.id.activity_due_show_amount)

            nameElement.text = it.name
            amountElement.text = it.balanceDue

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