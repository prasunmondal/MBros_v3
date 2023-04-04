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

class DataFetch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_fetch)
        val container = findViewById<LinearLayout>(R.id.data_fetch_entries_container)

        fetchData(container)
    }

    private fun fetchData(container: LinearLayout) {
        var list = listOf(
            GetCustomerOrders::get,
            CustomerKYC::getAllCustomers,
            CustomerData::getRecords,
            SingleAttributedData::getRecords,
            Refueling::get
        )

        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Fetching Orders"
                container.addView(uiEntry)
            }
            run(GetCustomerOrders::get)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()

        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Get Customer Profiles"
                container.addView(uiEntry)
            }
            run(CustomerKYC::getAllCustomers)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()


        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Customer Records"
                container.addView(uiEntry)
            }
            run(CustomerData::getRecords)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()

        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Today's Data"
                container.addView(uiEntry)
            }
            run(SingleAttributedData::getRecords)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()

        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Delivered Data"
                container.addView(uiEntry)
            }
            run(DeliverCustomerOrders::get)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()

        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Day Summary"
                container.addView(uiEntry)
            }
            run(DaySummary::get)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()

        Thread {
            var uiEntry: View? = null
            runOnUiThread {
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Fuel Data"
                container.addView(uiEntry)
            }
            run(Refueling::get)
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()
    }

    fun run(function: () -> (Unit)) {
        function.invoke()
    }

    fun run2(function: () -> (Unit)) {
        Thread {
            val uiEntry: View?
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
            uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = "Fuel Data"
            runOnUiThread {
                findViewById<LinearLayout>(R.id.data_fetch_entries_container).addView(uiEntry)
            }
            function.invoke()
            runOnUiThread {
                uiEntry?.findViewById<ConstraintLayout>(R.id.fragment_data_fetch_container)?.
                setBackgroundColor(ContextCompat.getColor(this, R.color.verify_delivery_valid))
            }
        }.start()
    }

    fun onClickDataFetchComplete(view: View) {
        super.onBackPressed()
    }
}