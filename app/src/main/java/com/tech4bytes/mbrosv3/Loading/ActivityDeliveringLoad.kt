package com.tech4bytes.mbrosv3.Loading

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.Delivering.ActivityDeliveringDeliver
import com.tech4bytes.mbrosv3.GetOrders.OrdersTotalModel
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
        addListeners()

        showLoadOrderData()
        if(isLoadingComplete()) {
            goToDeliveringDeliverPage()
        }
    }

    private fun showLoadOrderData() {
        val loadOrderObject = OrdersTotalModel.get(true)
        val loadOrderKg = loadOrderObject.requiredKg
        val loadOrderPc = loadOrderObject.requiredPc
        UIUtils.setUIElementValue(this, LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::requiredPc), loadOrderPc.toString())
        UIUtils.setUIElementValue(this, LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::requiredKg), loadOrderKg.toString())
    }

    private fun addListeners() {
        (LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::actualPc) as EditText).addTextChangedListener {
            updateAvgWeightText()
        }

        (LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::actualKg) as EditText).addTextChangedListener {
            updateAvgWeightText()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateAvgWeightText() {
        try {
            LogMe.log((LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::actualKg) as EditText).text.toString())
            LogMe.log((LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::actualPc) as EditText).text.toString())
            val actualKg = UIUtils.getUIElementValue(LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::actualKg)).toDouble()
            val actualPc = UIUtils.getUIElementValue(LoadingOrdersTotalModel.getUiElement(view, OrdersTotalModel::actualPc)).toInt()
            val avgWt = actualKg/actualPc
            UIUtils.setUIElementValue(this, view.findViewById<TextView>(R.id.activity_delivering_load_actualAvgWt), format("%.3f", avgWt))
        } catch (e: NumberFormatException) {
            LogMe.log(e)
            UIUtils.setUIElementValue(this, view.findViewById<TextView>(R.id.activity_delivering_load_actualAvgWt), "--.---")
        }

    }

    private fun isLoadingComplete(): Boolean {
        return false
    }

    fun onClickDeliverButton(view: View) {
        goToDeliveringDeliverPage()
    }

    fun goToDeliveringDeliverPage() {
        val switchActivityIntent = Intent(this, ActivityDeliveringDeliver::class.java)
        startActivity(switchActivityIntent)
    }
}