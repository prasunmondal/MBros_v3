package com.tech4bytes.mbrosv3.Sms.OneShotSMS

import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.AppUsers.RolesUtils

data class OSMSModel(
    val commReceiverCategory: String,
    val platform: String,
    val sendTo: String,
    val communicationType: String,
    val inputData: String,
    val dataTemplate: String,
    val enablement_template: String,
    val isEnabled: String,
    val auth: String
) : java.io.Serializable {

    override fun toString(): String {
        return "OSMSModel(platform='$platform', sendTo='$sendTo', communicationType='$communicationType', inputData='$inputData', dataTemplate='$dataTemplate', enablement_template='$enablement_template', isEnabled='$isEnabled')"
    }

    fun isMsgEnabled(smsModel: OSMSModel): Boolean {
        return smsModel.isEnabled.toBoolean()
    }

    fun isAuthorized(): Boolean {
        val authStrings = this.auth.split(",")
        authStrings.forEach { authStr ->
            if (AuthorizationUtils.isAuthorized(authStr.trim())) {
                return true
            }
        }
        return false
    }
}

data class SMS(
    val medium: String,
    val number: String,
    val text: String,
    var isEnabled: Boolean = true,
) : java.io.Serializable {

    override fun toString(): String {
        return "${medium.toUpperCase()}: $number ($isEnabled) - $text"
    }
}