package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import com.tech4bytes.mbrosv3.Loading.LoadModel
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    lateinit var containerView: View
    var uiEntriesList = mutableListOf<View>()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        containerView = binding.activityGetOrderEstimatesOrderListContainer
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = Math.random().toString()
//        }


//        containerView = findViewById<LinearLayout>(R.id.activity_get_order_estimates__parent_view)

//        populateCustomerList()

        CustomerKYC.getAllCustomers().forEach { masterList ->
            var isInOrderList = false
            GetCustomerOrders.get().forEach { orderList ->
                if(masterList.nameEng == orderList.name) {
                    createEstimatesView(orderList)
                    isInOrderList = true
                }
            }
            if(!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                createEstimatesView(masterList.nameEng)
            }
        }
//        setTotalLoadOrder()
        updateTotalPc()
        updateTotalKg()

        return root
    }

    private fun setTotalLoadOrder() {
        val metadataObj = SingleAttributedData.getRecords()
        val kg = metadataObj.estimatedLoadKg + ""
        val pc = metadataObj.estimatedLoadPc + ""

        UIUtils.setUIElementValue(AppContexts.get(), binding.activityGetOrderEstimatesTotalKg, kg)
        UIUtils.setUIElementValue(AppContexts.get(), binding.activityGetOrderEstimatesTotalPc, pc)
    }

    private fun createEstimatesView(customerName: String) {
        createEstimatesView(GetCustomerOrders(name = customerName))
    }

    private fun createEstimatesView(order: GetCustomerOrders) {
        val listContainer = binding.activityGetOrderEstimatesOrderListContainer
        val layoutInflater = LayoutInflater.from(AppContexts.get())
        val entry = layoutInflater.inflate(R.layout.activity_get_order_estimates_fragment_customer_order, null)

        val pcElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc)
        val kgElement = entry.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg)
        UIUtils.setUIElementValue(AppContexts.get(), entry.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name), order.name)
        UIUtils.setUIElementValue(AppContexts.get(), pcElement, order.orderedPc)
        UIUtils.setUIElementValue(AppContexts.get(), kgElement, order.orderedKg)

        pcElement.doOnTextChanged { text, start, before, count ->
            updateTotalPc()
        }

        kgElement.doOnTextChanged { text, start, before, count ->
            updateTotalKg()
        }

//        val deleteBtn = entry.findViewById<ImageButton>(R.id.fragment_customer_order_delete_record_button)
//        deleteBtn.setOnClickListener {
//            uiEntriesList.remove(entry)
//            listContainer.removeView(entry)
//            updateTotalPc()
//            updateTotalKg()
//        }

        uiEntriesList.add(entry)
        listContainer.addView(entry)
    }

    private fun updateTotalKg() {
        var sum = 0.0
        uiEntriesList.forEach {
            val kg = "0${UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg))}"
            sum += kg.toDouble()
        }
        LogMe.log(sum.toString())
        UIUtils.setUIElementValue(AppContexts.get(), binding.activityGetOrderEstimatesTotalKg, "$sum kg")
    }

    private fun updateTotalPc() {
        LogMe.log("a")
        var sum = 0
        uiEntriesList.forEach {
            LogMe.log("b")
            val pc = "0${UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc))}"
            LogMe.log("c: $pc")
            sum += pc.toInt()
            LogMe.log("d: $sum")
        }
        LogMe.log("e: $sum.toString()")
        UIUtils.setUIElementValue(AppContexts.get(), binding.activityGetOrderEstimatesTotalPc, "$sum pc")
    }

    private fun getCustomerNamesAsStringList(): List<String> {
        val namesList = mutableListOf<String>()

        CustomerKYC.getAllCustomers().forEach {
            namesList.add(it.getDisplayName())
        }
        return namesList
    }

    fun createNGetObjectsFromUI(): List<GetCustomerOrders> {
        val list = mutableListOf<GetCustomerOrders>()
        uiEntriesList.forEach {
            val name = UIUtils.getUIElementValue(it.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name))
            val pc = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_pc))
            val kg = UIUtils.getUIElementValue(it.findViewById<AppCompatEditText>(R.id.fragment_customer_order_kg))
            val obj = GetCustomerOrders(id = System.currentTimeMillis().toString(),
                name = name,
                seqNo = "",
                orderedPc = pc,
                orderedKg = kg,
                rate = "",
                prevDue = "0")
            list.add(obj)
        }
        return list
    }

    private fun isOnList(name: String): Boolean {
        uiEntriesList.forEach {
            val nameFromList = UIUtils.getUIElementValue(it.findViewById<AppCompatTextView>(R.id.fragment_customer_order_name))
            if(nameFromList == name)
                return true
        }
        return false
    }

    fun onClickSaveBtn(view: View) {
        Toast.makeText(AppContexts.get(), "Saving Data", Toast.LENGTH_SHORT).show()
        GetCustomerOrders.deleteAll()
        GetCustomerOrders.save(createNGetObjectsFromUI())

        val metadataObj = SingleAttributedData.getRecords()
        metadataObj.estimatedLoadPc = UIUtils.getUIElementValue(LoadModel.getUiElementFromOrderingPage(view, LoadModel::requiredPc))
        metadataObj.estimatedLoadKg = UIUtils.getUIElementValue(LoadModel.getUiElementFromOrderingPage(view, LoadModel::requiredKg))

        SingleAttributedData.save(metadataObj)
        Toast.makeText(AppContexts.get(), "Data save complete!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}