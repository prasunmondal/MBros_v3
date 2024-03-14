package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.ActivityAuthEnums
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataHandler
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.OneShotSMS.OSMS
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.DB.clients.getMultipleTabs.GetMultipleTabs
import kotlin.reflect.KFunction

class DataFetchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_fetch)
        supportActionBar!!.hide()
        AppContexts.set(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val container = findViewById<LinearLayout>(R.id.data_fetch_entries_container)

        var nextActivity: Class<*>? = null
        var currentActivity: ActivityAuthEnums = ActivityAuthEnums.ONE_SHOT_DELIVERY
        if (intent.extras != null) {
            nextActivity = intent.getSerializableExtra("nextActivity") as Class<*>?
            currentActivity = intent.getSerializableExtra("currentActivity") as ActivityAuthEnums
        }
        fetchData(container, DataFetchingInfo.get(currentActivity), nextActivity)
    }

    private fun goToNextActivity(nextActivity: Class<*>) {
        val switchActivityIntent = Intent(this, nextActivity)
        startActivity(switchActivityIntent)
    }

    private fun fetchData(container: LinearLayout, executingMethods: ExecutingMethods, nextActivity: Class<*>?) {
        val map: MutableMap<KFunction<Any>, FetchData> = mutableMapOf()

        if (executingMethods.get().isEmpty() && nextActivity != null) {
            goToNextActivity(nextActivity)
        } else {
            executingMethods.get().forEach {
                val uiEntry: View
                val layoutInflater = LayoutInflater.from(AppContexts.get())
                uiEntry = layoutInflater.inflate(R.layout.activity_data_fetch_fragments, null)
                uiEntry?.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.text = DataFetchingInfo.getDescription(it.key)
                container.addView(uiEntry)
                map[it.key] = FetchData(uiEntry, DataFetchingInfo.getDescription(it.key), it.value.method, it.value.useCache, false)
            }

            map.forEach {
                @Suppress("UNCHECKED_CAST")
                run(map, it.key, it.value.useCache, nextActivity)
            }
        }
    }

    private fun run(list: MutableMap<KFunction<Any>, FetchData>, key: KFunction<Any>, useCache: Boolean, nextActivity: Class<*>?) { //uiEntry: View, function: (Boolean) -> (Unit)) {
        Thread {



            runOnUiThread {
                Toast.makeText(AppContexts.get(), "Fetching all data", Toast.LENGTH_SHORT).show()
            }

                val sheetClassMap: MutableMap<String, KFunction<Any>> = mutableMapOf()
                sheetClassMap["metadata"] = SingleAttributedDataUtils::parseAndSaveToCache
                sheetClassMap["customerDetails"] = CustomerKYC::parseAndSaveToCache
//            sheetClassMap["deliveries"] = CustomerData::parseAndSaveToLocal
                sheetClassMap["DeliverOrders"] = DeliverToCustomerDataHandler::parseAndSaveToCache

                GetMultipleTabs.builder().scriptId("https://script.google.com/macros/s/AKfycbyVdzZW7Bg5-tAFM4_LfWfBozea-OPyFQQrHMGkNJiqXBsEyMZXNlG-QbX5aF5VXABPZQ/exec")
                    .sheetId("1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w")
                    .tabName("metadata,customerDetails,DeliverOrders")
                    .SheetClassMapBuilder(sheetClassMap)
                    .build()
                    .execute()

//                GetMultipleTabs.builder().scriptId("https://script.google.com/macros/s/AKfycbyVdzZW7Bg5-tAFM4_LfWfBozea-OPyFQQrHMGkNJiqXBsEyMZXNlG-QbX5aF5VXABPZQ/exec")
//                    .sheetId("1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w")
//                    .tabName("smsModel,metadata")
//                    .SheetClassMapBuilder(sheetClassMap)
//                    .build()
//                    .execute()
            runOnUiThread {
                Toast.makeText(AppContexts.get(), "Fetching all data complete", Toast.LENGTH_SHORT).show()
            }




            @Suppress("UNCHECKED_CAST")
            list[key]!!.executingMethod.invoke()
//            (key as ((Boolean) -> Unit)).invoke(useCache)
            runOnUiThread {
                list[key]!!.view.findViewById<TextView>(R.id.fragment_data_fetch_task_name)?.setTextColor(ContextCompat.getColor(this, R.color.delivery_input_valid))
                list[key]!!.isCompleted = true

                var allCompleted = true
                var countCompletedExecutions = 0
                list.forEach {
                    if (!it.value.isCompleted) {
                        allCompleted = false
                    } else {
                        countCompletedExecutions += 1
                    }
                }
                findViewById<TextView>(R.id.osl_fetching_data_progress_count_label).text = "Fetching Data: ${countCompletedExecutions}/${list.size}"
                if (allCompleted && nextActivity != null) {
                    goToNextActivity(nextActivity)
                }
            }
        }.start()
    }
}