package com.tech4bytes.mbrosv3.Loading

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.Delivering.ActivityDeliveringDeliver
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

        if(isLoadingComplete()) {
            goToDeliveringDeliverPage()
        }
    }

    private fun addListeners() {
        (LoadingDataModel.getUiElement(view, LoadingDataModel::actualPc) as EditText).addTextChangedListener {
            updateAvgWeightText()
        }

        (LoadingDataModel.getUiElement(view, LoadingDataModel::actualKg) as EditText).addTextChangedListener {
            updateAvgWeightText()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateAvgWeightText() {
        try {
            LogMe.log((LoadingDataModel.getUiElement(view, LoadingDataModel::actualKg) as EditText).text.toString())
            LogMe.log((LoadingDataModel.getUiElement(view, LoadingDataModel::actualPc) as EditText).text.toString())
            val actualKg = UIUtils.getUIElementValue(LoadingDataModel.getUiElement(view, LoadingDataModel::actualKg)).toDouble()
            val actualPc = UIUtils.getUIElementValue(LoadingDataModel.getUiElement(view, LoadingDataModel::actualPc)).toInt()
            val avgWt = actualKg/actualPc
            UIUtils.setUIElementValue(this, LoadingDataModel.getUiElement(view, LoadingDataModel::avgWeight), format("%.3f", avgWt))
        } catch (e: NumberFormatException) {
            LogMe.log(e)
            UIUtils.setUIElementValue(this, LoadingDataModel.getUiElement(view, LoadingDataModel::avgWeight), "--.---")
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