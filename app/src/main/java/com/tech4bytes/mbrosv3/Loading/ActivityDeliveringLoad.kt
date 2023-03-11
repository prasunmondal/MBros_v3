package com.tech4bytes.mbrosv3.Loading

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.listOrders.ActivityDeliveringListOrders
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.lang.String.format

class ActivityDeliveringLoad : AppCompatActivity() {

    lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivering_load)
        view = findViewById(R.id.activity_delivering_load)
        AppContexts.set(this, this)
        AppUtils.logError()

        addListeners()
        showLoadOrderData()
//        if(isLoadingComplete(loadObj)) {
//            goToDeliveringDeliverPage()
//        }
    }

    private fun showLoadOrderData() {
        val metadataObj = SingleAttributedData.getRecords()
        val loadOrderPc = metadataObj.estimatedLoadPc
        val loadOrderKg = metadataObj.estimatedLoadKg
        val loadActualPc = metadataObj.actualLoadPc
        val loadActualKg = metadataObj.actualLoadKg

        UIUtils.setUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::requiredPc), loadOrderPc)
        UIUtils.setUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::requiredKg), loadOrderKg)
        UIUtils.setUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualPc), loadActualPc)
        UIUtils.setUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualKg), loadActualKg)
    }

    private fun addListeners() {
        (LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualPc) as EditText).addTextChangedListener {
            updateAvgWeightText()
        }

        (LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualKg) as EditText).addTextChangedListener {
            updateAvgWeightText()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateAvgWeightText() {
        try {
            LogMe.log((LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualKg) as EditText).text.toString())
            LogMe.log((LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualPc) as EditText).text.toString())
            val actualKg = UIUtils.getUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualKg)).toDouble()
            val actualPc = UIUtils.getUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualPc)).toInt()
            val avgWt = actualKg/actualPc
            UIUtils.setUIElementValue(view.findViewById<TextView>(R.id.activity_delivering_load_actualAvgWt), format("%.3f", avgWt))
        } catch (e: NumberFormatException) {
            LogMe.log(e)
            UIUtils.setUIElementValue(view.findViewById<TextView>(R.id.activity_delivering_load_actualAvgWt), "--.---")
        }

    }

    fun onClickDeliverButton(view: View) {
        saveLoadActuals()
        goToDeliveringDeliverPage()
    }

    private fun saveLoadActuals() {
        val record = SingleAttributedData.getRecords()
        record.actualLoadPc = UIUtils.getUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualPc))
        record.actualLoadKg = UIUtils.getUIElementValue(LoadModel.getUiElementFromLoadingPage(view, LoadModel::actualKg))

        val finalFarmRateFromUI = findViewById<EditText>(R.id.activity_delivering_load_finalFarmRate).text.toString()
        val bufferRateFromUI = findViewById<EditText>(R.id.activity_delivering_load_bufferRate).text.toString()
        if(finalFarmRateFromUI.isNotEmpty())
            record.finalFarmRate = finalFarmRateFromUI
        if(bufferRateFromUI.isNotEmpty())
            record.bufferRate = bufferRateFromUI

        SingleAttributedData.save(record)
    }

    fun goToDeliveringDeliverPage() {
        val switchActivityIntent = Intent(AppContexts.get(), ActivityDeliveringListOrders::class.java)
        AppContexts.get().startActivity(switchActivityIntent)
        finish()
    }

    companion object {
        fun isLoadingComplete(loadObj: LoadModel): Boolean {
            return (loadObj != null && loadObj.isDone())
        }
    }

    fun activity_delivered_load_sync_data(view: View) {
        AppUtils.invalidateAllDataAndRestartApp()
    }
}