package com.tech4bytes.mbrosv3.Sms.OneShotSMS

data class OSMSModel(
    val commReceiverCategory: String,
    val platform: String,
    val sendTo: String,
    val communicationType: String,
    val inputData: String,
    val dataTemplate: String,
    val enablement_template: String,
    val isEnabled: String,
) : java.io.Serializable {

    override fun toString(): String {
        return "OSMSModel(platform='$platform', sendTo='$sendTo', communicationType='$communicationType', inputData='$inputData', dataTemplate='$dataTemplate', enablement_template='$enablement_template', isEnabled='$isEnabled')"
    }
}

data class SMS(
    val medium: String,
    val number: String,
    val text: String,
    var isEnabled: Boolean = true,
) : java.io.Serializable