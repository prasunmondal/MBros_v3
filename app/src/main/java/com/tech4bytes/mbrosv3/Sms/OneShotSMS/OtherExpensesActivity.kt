package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.OneShot.RefuelUI
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class OtherExpensesActivity : AppCompatActivity() {
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
        Thread {
            RefuelUI.initializeFinalKm(this, findViewById(R.id.oe_final_km_elements_container))
            RefuelUI.initiallizeRefuelUI(this, findViewById(R.id.oe_refuel_elements_container))
        }.start()
    }
}