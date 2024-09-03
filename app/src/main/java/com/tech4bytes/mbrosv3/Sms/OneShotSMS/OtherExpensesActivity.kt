package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.OneShot.RefuelUI
import com.tech4bytes.mbrosv3.R
import com.prasunmondal.dev.libs.contexts.AppContexts

class OtherExpensesActivity : AppCompatActivity() {
    private lateinit var refuelUIObj: RefuelUI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_expenses)
        AppContexts.set(this)
        AppUtils.logError(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        initializeUI()
    }

    private fun initializeUI() {
        refuelUIObj = RefuelUI(this, findViewById(R.id.oe_final_km_elements_container), findViewById(R.id.oe_refuel_elements_container))
        Thread {
            refuelUIObj.initializeFinalKm()
            refuelUIObj.initiallizeRefuelUI()
        }.start()
    }

    fun onClickSaveBtn(view: View) {
        val saveBtn = findViewById<TextView>(R.id.oe_save_btn)
        Thread {
            runOnUiThread {
                saveBtn.text = "Saving Data..."
                saveBtn.isEnabled = false
            }
            refuelUIObj.saveDataFromThisUI(true)
            runOnUiThread {
                saveBtn.text = "Save"
                saveBtn.isEnabled = true
            }
        }.start()
    }

    fun goToSendSMSPage(view: View) {
        val switchActivityIntent = Intent(this, OneShotSMS::class.java)
        val bundle = Bundle()
        bundle.putString("communication_selector_type", "Shareholder")
        bundle.putBoolean("useCache", false)
        switchActivityIntent.putExtras(bundle)
        startActivity(switchActivityIntent)
    }
}