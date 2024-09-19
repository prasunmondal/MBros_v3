//package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.AppCompatTextView
//import com.prasunmondal.dev.libs.contexts.AppContexts
//import com.prasunmondal.dev.libs.gsheet.clients.GScript
//import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
//import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDueData
//import com.tech4bytes.mbrosv3.R
//import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
//
//class OrdersMakeList : AppCompatActivity() {
//
//    lateinit var listOrders: List<GetCustomerOrderModel>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_orders_make_list)
//        AppContexts.set(this)
//
//        listOrders = GetCustomerOrderUtils.fetchAll().execute()
//        showList()
//    }
//
//    private fun showList() {
//        listOrders.forEach {
//            if ((it.orderedPc.isNotEmpty() && it.orderedPc.toInt() > 0) || (it.orderedKg.isNotEmpty() && it.orderedKg.toInt() > 0)) {
//                addEntry(it)
//            }
//        }
//    }
//
//    private fun addEntry(order: GetCustomerOrderModel) {
//        val listContainer = findViewById<LinearLayout>(R.id.order_make_list_container)
//        val layoutInflater = LayoutInflater.from(AppContexts.get())
//        val entry = layoutInflater.inflate(R.layout.activity_orders_make_list_fragments, null)
//
//        val elementPc = entry.findViewById<TextView>(R.id.make_list_fragment_pc)
//        val elementKg = entry.findViewById<TextView>(R.id.make_list_fragment_kg)
//        val elementName = entry.findViewById<AppCompatTextView>(R.id.make_list_fragment_name)
//        val elementDue = entry.findViewById<TextView>(R.id.make_list_fragment_due)
//
//        elementPc.text = getFinalPc(order)
//        elementKg.text = getFinalKg(order)
//        elementName.text = order.name
//        elementDue.text = CustomerDueData.getBalance(order.name).toString()
//
//        listContainer.addView(entry)
//    }
//
//    private fun getFinalPc(order: GetCustomerOrderModel): String {
//        return if (NumberUtils.getIntOrZero(order.calculatedPc) == 0) order.getEstimatedPc(false) else order.calculatedPc
//    }
//
//    private fun getFinalKg(order: GetCustomerOrderModel): String {
//        return if (NumberUtils.getIntOrZero(order.calculatedKg) == 0) order.getEstimatedKg(false) else order.calculatedKg
//    }
//
////    fun onClickGoToOrdersGetPage(view: View) {
////        val switchActivityIntent = Intent(this, ActivityGetCustomerOrders::class.java)
////        startActivity(switchActivityIntent)
////        finish()
////    }
//
//    fun onClickGoToOrdersFinalizePage(view: View) {
//        val switchActivityIntent = Intent(this, GetOrdersFinalize::class.java)
//        startActivity(switchActivityIntent)
//        finish()
//    }
//
//    fun onClickSaveBtn(view: View) {
//        Thread {
//            runOnUiThread {
//                Toast.makeText(this, "Saving Data", Toast.LENGTH_SHORT).show()
//            }
//            GetCustomerOrderUtils.save()
//            val metadataObj = SingleAttributedDataUtils.getRecords()
//            var totalPc = 0
//            var totalKg = 0
//            GetCustomerOrderUtils.getListOfOrderedCustomers().forEach {
//                totalPc += NumberUtils.getIntOrZero(getFinalPc(it))
//                totalKg += NumberUtils.getIntOrZero(getFinalKg(it))
//            }
//            metadataObj.estimatedLoadPc = totalPc.toString()
//            metadataObj.estimatedLoadKg = totalKg.toString()
//
//            SingleAttributedDataUtils.insert(metadataObj).queue()
//            runOnUiThread {
//                Toast.makeText(this, "Data save complete!", Toast.LENGTH_LONG).show()
//            }
//            GScript.execute()
//        }.start()
//    }
//}