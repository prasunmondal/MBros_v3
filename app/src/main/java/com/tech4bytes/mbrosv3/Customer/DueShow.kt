package com.tech4bytes.mbrosv3.Customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class DueShow : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_due_show)
        AppContexts.set(this, this)

        showDues()
    }

    fun showDues() {
        val listContainer = findViewById<LinearLayout>(R.id.activity_due_show_fragment_conntainer)
        CustomerData.getAllLatestRecords().forEach {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_due_show_entry, null)

            var nameElement = entry.findViewById<TextView>(R.id.activity_due_show_name)
            var amountElement = entry.findViewById<TextView>(R.id.activity_due_show_amount)

            nameElement.text = it.name
            amountElement.text = it.balanceDue

            listContainer.addView(entry)
        }
    }
}