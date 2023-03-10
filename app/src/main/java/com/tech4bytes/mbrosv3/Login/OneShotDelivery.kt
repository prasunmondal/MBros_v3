package com.tech4bytes.mbrosv3.Login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Loading.LoadModel
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class OneShotDelivery : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_shot_delivery)

        AppContexts.set(this, this)
        AppUtils.logError()

        setGlobalListeners()
        showOrders(GetCustomerOrders.getListOfOrderedCustomers(), R.id.one_shot_delivery_entry_container)
//        showOrders(GetCustomerOrders.getListOfUnOrderedCustomers(), R.id.activity_delivering_deliver_unorder_list)
    }

    private fun setGlobalListeners() {
        val loadPcElement = findViewById<EditText>(R.id.one_shot_delivery_pc)
        val loadKgElement = findViewById<EditText>(R.id.one_shot_delivery_kg)
        val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
        val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)

        loadPcElement.setOnClickListener {
            val record = SingleAttributedData.getRecords()
            record.actualLoadPc = loadPcElement.text.toString()
            SingleAttributedData.save(record)
        }

        loadKgElement.setOnClickListener {
            val record = SingleAttributedData.getRecords()
            record.actualLoadKg = loadKgElement.text.toString()
            SingleAttributedData.save(record)
        }

        loadPriceElement.setOnClickListener {
            val record = SingleAttributedData.getRecords()
            record.finalFarmRate = loadPriceElement.text.toString()
            SingleAttributedData.save(record)
        }

        loadBufferElement.setOnClickListener {
            val record = SingleAttributedData.getRecords()
            record.bufferRate = loadBufferElement.text.toString()
            SingleAttributedData.save(record)
        }
    }

    fun showOrders(listOfCustomers: List<GetCustomerOrders>, container: Int) {
        listOfCustomers.forEach { order ->
            LogMe.log(order.toString())

            val listContainer = findViewById<LinearLayout>(container)
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_one_shot_delivery_fragment, null)

            val nameElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_name)
            val rateElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_rate)
            val pcElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)

            nameElement.text = order.name
            LogMe.log(SingleAttributedData.getFinalRateInt().toString())
            LogMe.log(SingleAttributedData.getBufferRateInt().toString())
            LogMe.log(CustomerKYC.get(order.name)!!.rateDifference)
            LogMe.log("${SingleAttributedData.getFinalRateInt() + SingleAttributedData.getBufferRateInt() + CustomerKYC.get(order.name)!!.rateDifference.toInt()}")
            rateElement.text = "${SingleAttributedData.getFinalRateInt() + SingleAttributedData.getBufferRateInt() + CustomerKYC.get(order.name)!!.rateDifference.toInt()}"


//            entry.setOnClickListener {
//                goTo_ActivityDeliveringDeliver(order.name)
//            }
//
//            if (isDelivered(order.name)) {
//                entry.findViewById<LinearLayout>(R.id.activity_due_show_fragment_conntainer).setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_completed))
//            }

            listContainer.addView(entry)
        }
    }

}