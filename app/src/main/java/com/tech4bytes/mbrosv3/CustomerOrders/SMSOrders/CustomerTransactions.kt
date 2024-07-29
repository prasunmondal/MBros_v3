package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CustomerAddTransactionActivity
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe


class CustomerTransactions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_transactions)
        AppContexts.set(this)
        AppUtils.logError(this)

        listCustomerNames()
    }

    private fun listCustomerNames() {
        val customerNamesSpinner = findViewById<Spinner>(R.id.ct_customer_names_spinner)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, CustomerDataUtils.getAllCustomerNames()
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        customerNamesSpinner.setAdapter(adapter)

        customerNamesSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long,
            ) {
                showTransactions(customerNamesSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                showTransactions("")
            }
        })
    }

    private fun showTransactions(name: String) {
        LogMe.startMethod()
        val list = CustomerDataUtils.get().filter { it.customerAccount == name || it.name == name }.sortedBy { it.orderId }.reversed()
        list.forEach {
            LogMe.log(it.toString())
        }
        val listContainer = findViewById<LinearLayout>(R.id.ct_list_container)
        listContainer.removeAllViews()
        addHeader(listContainer)

        Thread {
            list.forEach {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                val entry = layoutInflater.inflate(R.layout.activity_customer_transaction_entry, null)
                val t = DateUtils.getDate(it.timestamp)
                val formattedDate = DateUtils.getDateInFormat(t!!, "dd/MM")
                entry.findViewById<TextView>(R.id.ct_date).text = formattedDate
                entry.findViewById<TextView>(R.id.ct_name).text = it.name
                entry.findViewById<TextView>(R.id.ct_pc).text = it.deliveredPc
                entry.findViewById<TextView>(R.id.ct_kg).text = it.deliveredKg
                entry.findViewById<TextView>(R.id.ct_debit).text = it.deliverAmount
                entry.findViewById<TextView>(R.id.ct_credit).text = it.paid
                entry.findViewById<TextView>(R.id.ct_balance).text = it.khataBalance

                runOnUiThread {
                    listContainer.addView(entry)
                }
            }
        }.start()
    }

    private fun addHeader(listContainer: LinearLayout) {
        // add header
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_customer_transaction_entry, null)
        entry.findViewById<TextView>(R.id.ct_date).text = "Date"
        entry.findViewById<TextView>(R.id.ct_date).setTypeface(null, Typeface.BOLD)
        entry.findViewById<TextView>(R.id.ct_name).text = "Name"
        entry.findViewById<TextView>(R.id.ct_name).setTypeface(null, Typeface.BOLD)
        entry.findViewById<TextView>(R.id.ct_pc).text = "Pc"
        entry.findViewById<TextView>(R.id.ct_pc).setTypeface(null, Typeface.BOLD)
        entry.findViewById<TextView>(R.id.ct_kg).text = "Kg"
        entry.findViewById<TextView>(R.id.ct_kg).setTypeface(null, Typeface.BOLD)
        entry.findViewById<TextView>(R.id.ct_debit).text = "Sale"
        entry.findViewById<TextView>(R.id.ct_debit).setTypeface(null, Typeface.BOLD)
        entry.findViewById<TextView>(R.id.ct_credit).text = "Credit"
        entry.findViewById<TextView>(R.id.ct_credit).setTypeface(null, Typeface.BOLD)
        entry.findViewById<TextView>(R.id.ct_balance).text = "Balance"
        entry.findViewById<TextView>(R.id.ct_balance).setTypeface(null, Typeface.BOLD)
        listContainer.addView(entry)
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}