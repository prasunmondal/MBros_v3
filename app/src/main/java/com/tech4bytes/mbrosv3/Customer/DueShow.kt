package com.tech4bytes.mbrosv3.Customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.Login.ActivityLogin
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

            var nameElement = entry.findViewById<TextView>(R.id.fragment_actibity_login_roles_role)
            var amountElement = entry.findViewById<TextView>(R.id.activity_due_show_amount)

            nameElement.text = it.name
            amountElement.text = it.balanceDue

            listContainer.addView(entry)
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}