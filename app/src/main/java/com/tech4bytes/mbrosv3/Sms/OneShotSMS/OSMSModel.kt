package com.tech4bytes.mbrosv3.Sms.OneShotSMS

data class OSMSModel(
    val platform: String,
    val sendTo: String,
    val communicationType: String,
    val inputData: String,
    val dataTemplate: String,
    val isEnabled: String,
) : java.io.Serializable

data class SMS(val medium: String,
               val number: String,
               val text: String,
               var isEnabled: Boolean = true): java.io.Serializable