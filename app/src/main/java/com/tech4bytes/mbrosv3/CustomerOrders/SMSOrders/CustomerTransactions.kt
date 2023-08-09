package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class CustomerTransactions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_transactions)
        AppContexts.set(this)
        AppUtils.logError()

        val name = "Prabir"
        showTransactions(name)
    }

    private fun showTransactions(name: String) {
        var list = CustomerData.getRecords().filter { it.name == name }
        val listContainer = findViewById<LinearLayout>(R.id.ct_list_container)

        list.forEach {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_customer_transaction_entry, null)
            entry.findViewById<TextView>(R.id.ct_date).text = it.timestamp.split("T")[0]
            entry.findViewById<TextView>(R.id.ct_name).text = it.name
            entry.findViewById<TextView>(R.id.ct_pc).text = it.deliveredPc
            entry.findViewById<TextView>(R.id.ct_kg).text = it.deliveredKg
            entry.findViewById<TextView>(R.id.ct_debit).text = it.deliveredAmount
            entry.findViewById<TextView>(R.id.ct_credit).text = it.paid
            entry.findViewById<TextView>(R.id.ct_balance).text = it.balanceDue

            listContainer.addView(entry)
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}