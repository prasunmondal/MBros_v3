package com.tech4bytes.mbrosv3.Login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
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

        AppContexts.set(this)
        AppUtils.logError()

        showOrders()
    }

    fun showOrders() {
        showOrders(GetCustomerOrders.getListOfOrderedCustomers(), R.id.one_shot_delivery_entry_container)
//        showOrders(GetCustomerOrders.getListOfUnOrderedCustomers(), R.id.activity_delivering_deliver_unorder_list)
    }

    fun showOrders(listOfCustomers: List<GetCustomerOrders>, container: Int) {

        val listContainer = findViewById<LinearLayout>(container)
        listContainer.removeAllViews()

        listOfCustomers.forEach { order ->
            LogMe.log(order.toString())

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

    fun onClickSyncInputsOffline(view: View) {
        val record = SingleAttributedData.getRecords()
        val loadPcElement = findViewById<EditText>(R.id.one_shot_delivery_pc)
        val loadKgElement = findViewById<EditText>(R.id.one_shot_delivery_kg)
        val loadPriceElement = findViewById<EditText>(R.id.one_shot_delivery_price)
        val loadBufferElement = findViewById<EditText>(R.id.one_shot_delivery_buffer)

        record.actualLoadPc = loadPcElement.text.toString()
        record.actualLoadKg = loadKgElement.text.toString()
        record.finalFarmRate = loadPriceElement.text.toString()
        record.bufferRate = loadBufferElement.text.toString()
        SingleAttributedData.saveToLocal(record)
        showOrders()
    }

}