package com.tech4bytes.mbrosv3.Sms.OneShotSMS

data class OSMSModel(val platform: String,
                     val number: String,
                     val sms_type: String,
                     val variable: String,
                     val data: String,
                     val enabled: String): java.io.Serializable
