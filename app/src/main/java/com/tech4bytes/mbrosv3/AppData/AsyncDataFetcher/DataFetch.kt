package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling
import kotlin.reflect.KFunction

class DataFetch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_fetch)
        val container = findViewById<LinearLayout>(R.id.data_fetch_entries_container)

        fetchData(container)
    }

    private fun fetchData(container: LinearLayout) {
        val list = listOf(
            GetCustomerOrders::get,
            CustomerKYC::getAllCustomers,
            CustomerData::getRecords,
            SingleAttributedData::getRecords,
            DeliverCustomerOrders::get,
            DaySummary::get,
            Refueling::get
        )

        val map: MutableMap<KFunction<Any>, View> = mutableMapOf()

        list.forEach {
            val uiEntry: View
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
            uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = it.name
            container.addView(uiEntry)
            map[it] = uiEntry
        }

        map.forEach {
            Thread {
                @Suppress("UNCHECKED_CAST")
                run(it.value, it.key as ((Boolean) -> Unit))
            }.start()
        }
    }

    private fun run(uiEntry: View, function: (Boolean) -> (Unit)) {
        Thread {
            function.invoke(true)
            runOnUiThread {
                uiEntry.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()
    }

    fun onClickDataFetchComplete(view: View) {
        super.onBackPressed()
    }
}