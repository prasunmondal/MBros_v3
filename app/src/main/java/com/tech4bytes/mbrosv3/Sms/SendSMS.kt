package com.tech4bytes.mbrosv3.Sms

//import android.app.Activity
//import android.app.PendingIntent
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.telephony.SmsManager
//import android.widget.Toast
//import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
//
//
//class SendSMS: BroadcastReceiver() {
//
//    private fun sendSMS(phoneNumber: String, message: String) {
//        val SENT = "SMS_SENT"
//        val DELIVERED = "SMS_DELIVERED"
//        val sentPI = PendingIntent.getBroadcast(
//            AppContexts.get(), 0,
//            Intent(SENT), 0
//        )
//        val deliveredPI = PendingIntent.getBroadcast(
//            AppContexts.get(), 0,
//            Intent(DELIVERED), 0
//        )
//
//        //---when the SMS has been sent---
//        registerReceiver(object : BroadcastReceiver() {
//            override fun onReceive(arg0: Context?, arg1: Intent?) {
//                when (resultCode) {
//                    Activity.RESULT_OK -> Toast.makeText(
//                        AppContexts.get(), "SMS sent",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(
//                        AppContexts.get(), "Generic failure",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(
//                        AppContexts.get(), "No service",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(
//                        AppContexts.get(), "Null PDU",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(
//                        AppContexts.get(), "Radio off",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }, IntentFilter(SENT))
//
//        //---when the SMS has been delivered---
//        (object : BroadcastReceiver() {
//            override fun onReceive(arg0: Context?, arg1: Intent?) {
//                when (resultCode) {
//                    Activity.RESULT_OK -> Toast.makeText(
//                        AppContexts.get(), "SMS delivered",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Activity.RESULT_CANCELED -> Toast.makeText(
//                        AppContexts.get(), "SMS not delivered",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }, IntentFilter(DELIVERED))
//        val sms: SmsManager = SmsManager.getDefault()
//        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI)
//    }
//
//    override fun onReceive(p0: Context?, p1: Intent?) {
//
//    }
//}
//
//
//internal class deliverReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, arg1: Intent) {
//        when (resultCode) {
//            Activity.RESULT_OK -> Toast.makeText(
//                AppContexts.get(), "sms_delivered",
//                Toast.LENGTH_SHORT
//            ).show()
//            Activity.RESULT_CANCELED -> Toast.makeText(
//                AppContexts.get(), "sms_not_delivered",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//}