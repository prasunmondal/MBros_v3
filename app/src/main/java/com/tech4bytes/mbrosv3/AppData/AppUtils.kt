package com.tech4bytes.mbrosv3.AppData

import android.content.Intent
import android.widget.Toast
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class AppUtils {

    companion object {

        fun invalidateAllDataAndRestartApp() {
            Toast.makeText(AppContexts.get(), "Refreshing data", Toast.LENGTH_SHORT).show()
            CentralCache.invalidateFullCache()
            goToLoginPage()
        }

        private fun goToLoginPage() {
            val switchActivityIntent = Intent(AppContexts.get(), ActivityLogin::class.java)
            AppContexts.get().startActivity(switchActivityIntent)
        }
    }
}