package com.tech4bytes.mbrosv3.AppData

import android.util.Log
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.BuildConfig
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

data class AppVersionModel(val commitId: String, val updateName: String, val downloadLink: String): java.io.Serializable

object AppVersion: GSheetSerialized<AppVersionModel>(
    ContextWrapper(AppContexts.get()),
    ProjectConfig.dBServerScriptURLNew,
    ProjectConfig.get_db_sheet_id(),
    "appUpdates",
    query = null,
    modelClass = AppVersionModel::class.java
) {

    fun getUpdateLink(updateCommitId: String, useCache: Boolean = false): String? {
        AppVersion.fetchAll(useCache).execute().forEach {
            if(it.commitId == updateCommitId)
                return it.downloadLink
        }
        return null
    }

    fun isUpdateAvailable(recommendedAppVersion: String): Boolean {
        LogMe.log("RV: $recommendedAppVersion, BC: ${getBuildCommit()}")
        if(recommendedAppVersion == "")
            return false
        if(recommendedAppVersion == getBuildCommit())
            return false
        return true
    }

    fun getAppVersionString(): String {
        return "App Version: ${getBuildCommit()} (${getBuildCommitDate()})"
    }

    fun getBuildCommit(): String {
        return BuildConfig.lastGitCommitHash
    }

    fun getBuildCommitDate(): String {
        return BuildConfig.lastGitCommitDate
    }
}


// RolesUtils.getAppUser()!!.recommended_app_version