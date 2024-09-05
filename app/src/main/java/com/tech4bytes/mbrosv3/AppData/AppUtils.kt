package com.tech4bytes.mbrosv3.AppData

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.prasunmondal.dev.libs.caching.CentralCacheObj
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.errorHandling.ErrorHandler
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.UserRoleUtils
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.ProjectConfig


class AppUtils {

    companion object {

        fun invalidateAllDataAndRestartApp() {
            UserRoleUtils.deleteUserRolesCache()
            CentralCacheObj.centralCache.invalidateFullCache(AppContexts.get())
            goToLoginPage()
        }

        private fun goToLoginPage() {
            val myIntent = Intent(AppContexts.get(), ActivityLogin::class.java)
            AppContexts.get().startActivity(myIntent)
        }

        fun logError(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler { thread, e ->
                ErrorHandler().reportError(context, ProjectConfig.get_db_sheet_id(), "errors-logs", e)
                Toast.makeText(context, "Error Occurred. Logging to sheet", Toast.LENGTH_SHORT).show()
                throw e
            }
        }

        fun showWaitDialog(title: String, message: String): ProgressDialog? {
            return ProgressDialog.show(AppContexts.get(), title, message, true)
        }
    }
}