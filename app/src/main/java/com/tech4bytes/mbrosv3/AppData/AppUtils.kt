package com.tech4bytes.mbrosv3.AppData

import android.app.ProgressDialog
import android.content.Intent
import android.widget.Toast
import com.prasunmondal.postjsontosheets.clients.post.raw.PostSequence
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts


class AppUtils {

    companion object {

        fun invalidateAllDataAndRestartApp() {
            Toast.makeText(AppContexts.get(), "Refreshing data", Toast.LENGTH_SHORT).show()
            CentralCache.invalidateFullCache()
            goToLoginPage()
        }

        private fun goToLoginPage() {
            val myIntent = Intent(AppContexts.get(), ActivityLogin::class.java)
            AppContexts.get().startActivity(myIntent)
        }

        private fun getStackTrace(ex: Exception): String? {
            val sb = StringBuilder()
            val st = ex.stackTrace
            sb.append(ex.javaClass.name).append(": ").append(ex.message).append("\n")
            for (stackTraceElement in st) {
                sb.append("\t at ").append(stackTraceElement.toString()).append("\n")
            }
            return sb.toString()
        }

        fun logError() {
            Thread.setDefaultUncaughtExceptionHandler { thread, e ->

                val str = """
                    < EXCEPTION START >
                    Exception: $e
                    Message: ${e.message}
                    --------------- Stacktrace ---------------
                    ${getStackTrace(e as Exception)}< EXCEPTION END >
                    """.trimIndent()

                Toast.makeText(AppContexts.get(), "Error Occurred. Logging to sheet", Toast.LENGTH_SHORT).show()
                PostSequence.builder()
                    .scriptId(ProjectConfig.dBServerScriptURL)
                    .sheetId(ProjectConfig.get_db_sheet_id())
                    .tabName("errors-logs")
                    .dataObject(str)
                    .build().execute()
                throw e
            }
        }

        fun showWaitDialog(title: String, message: String): ProgressDialog? {
            return ProgressDialog.show(AppContexts.get(), title, message, true)
        }
    }
}