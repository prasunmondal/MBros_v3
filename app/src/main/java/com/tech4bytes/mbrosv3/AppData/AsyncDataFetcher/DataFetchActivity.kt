package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.VehicleManagement.Refueling
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction

class DataFetchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_fetch)
        val container = findViewById<LinearLayout>(R.id.data_fetch_entries_container)

        var nextActivity: Class<*>? = null
        if (intent.extras != null) {
            nextActivity = intent.getSerializableExtra("nextActivity") as Class<*>?
        }
        fetchData(container, nextActivity)
    }

    private fun goToNextActivity(nextActivity: Class<*>) {
            val switchActivityIntent = Intent(this, nextActivity)
            startActivity(switchActivityIntent)
    }

    private fun fetchData(container: LinearLayout, nextActivity: Class<*>?) {
        val list = listOf(
            GetCustomerOrders::get,
            CustomerKYC::getAllCustomers,
            CustomerData::getRecords,
            SingleAttributedData::getRecords,
            DeliverCustomerOrders::get,
            DaySummary::get,
            Refueling::get
        )

        val map: MutableMap<KFunction<Any>, FetchData> = mutableMapOf()

        list.forEach {
            val uiEntry: View
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
            uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = it.name
            container.addView(uiEntry)
            map[it] = FetchData(uiEntry, it.name, it, false)
        }

        map.forEach {
            @Suppress("UNCHECKED_CAST")
            run(map, it.key, nextActivity)
        }
    }

    private fun run(list: MutableMap<KFunction<Any>, FetchData>, key: KFunction<Any>, nextActivity: Class<*>?) { //uiEntry: View, function: (Boolean) -> (Unit)) {
        Thread {
            @Suppress("UNCHECKED_CAST")
            (key as ((Boolean) -> Unit)).invoke(true)
            runOnUiThread {
                list[key]!!.view.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
                list[key]!!.isCompleted = true

                var allCompleted = true
                LogMe.log("==== Checking Completeness ====")
                list.forEach {
                    if(!it.value.isCompleted) {
                        LogMe.log(it.key.name + " : " + it.value.isCompleted)
                        allCompleted = false
                    }
                }
                LogMe.log("Final Verdict: $allCompleted")
                LogMe.log("==== ===================== ====")
                if (allCompleted && nextActivity != null) {
                    goToNextActivity(nextActivity)
                }
            }
        }.start()
    }

    fun onClickDataFetchComplete(view: View) {
        super.onBackPressed()
    }
}