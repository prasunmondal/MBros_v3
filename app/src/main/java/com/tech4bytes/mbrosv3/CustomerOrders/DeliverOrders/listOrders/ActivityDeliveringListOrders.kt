package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Loading.ActivityDeliveringLoad
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.VehicleManagement.ActivityGetFinalKm
import com.tech4bytes.mbrosv3.VehicleManagement.ActivityRefueling

class ActivityDeliveringListOrders : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_list)
        AppContexts.set(this, this)
        AppUtils.logError()

        showOrders(GetCustomerOrders.getListOfOrderedCustomers(), R.id.activity_delivering_deliver_order_list)
        showOrders(GetCustomerOrders.getListOfUnOrderedCustomers(), R.id.activity_delivering_deliver_unorder_list)
    }

    fun showOrders(listOfCustomers: List<GetCustomerOrders>, container: Int) {
        listOfCustomers.forEach { order ->
            LogMe.log(order.toString())

            val listContainer = findViewById<LinearLayout>(container)
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_delivering_list_fragment_order_details, null)

            val seqNoElement = entry.findViewById<AppCompatTextView>(R.id.activity_due_show_amount)
            val nameElement = entry.findViewById<AppCompatTextView>(R.id.fragment_actibity_login_roles_role)
            val pcElement = entry.findViewById<AppCompatTextView>(R.id.activity_delivering_deliver_fragment_order_pc)
            val kgElement = entry.findViewById<AppCompatTextView>(R.id.activity_delivering_deliver_fragment_order_kg)

            UIUtils.setUIElementValue(seqNoElement, order.seqNo + ".")
            UIUtils.setUIElementValue(nameElement, order.name)
            UIUtils.setUIElementValue(pcElement, order.orderedPc)
            UIUtils.setUIElementValue(kgElement, order.orderedKg)

            entry.setOnClickListener {
                goTo_ActivityDeliveringDeliver(order.name)
            }

            if (isDelivered(order.name)) {
                entry.findViewById<LinearLayout>(R.id.activity_due_show_fragment_conntainer).setBackgroundColor(ContextCompat.getColor(this, R.color.delivery_completed))
            }

            listContainer.addView(entry)
        }
    }

    fun isDelivered(name: String): Boolean {
        val obj = DeliverToCustomerCalculations.getByName(name)
        return (obj != null && obj.deliveryStatus == "DELIVERED")
    }

    private fun goTo_ActivityDeliveringDeliver(name: String) {
        val switchActivityIntent = Intent(this, DeliverToCustomerActivity::class.java)
        val bundle = Bundle()
        bundle.putString("name", name)
        switchActivityIntent.putExtras(bundle)
        startActivity(switchActivityIntent)
    }

    fun delivering_list_refresh_btn(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
    }

    fun onClickRefuelButton(view: View) {
        val switchActivityIntent = Intent(this, ActivityRefueling::class.java)
        startActivity(switchActivityIntent)
    }

    fun onClickDoneDeliveryButton(view: View) {
        val switchActivityIntent = Intent(this, ActivityGetFinalKm::class.java)
        startActivity(switchActivityIntent)
    }

    fun onClickGoToLoadPageButton(view: View) {
        val switchActivityIntent = Intent(this, ActivityDeliveringLoad::class.java)
        startActivity(switchActivityIntent)
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}