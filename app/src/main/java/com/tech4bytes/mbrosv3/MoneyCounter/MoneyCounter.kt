package com.tech4bytes.mbrosv3.MoneyCounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogUtils

class MoneyCounter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_counter)

        AppContexts.set(this)
        AppUtils.logError()
    }


}