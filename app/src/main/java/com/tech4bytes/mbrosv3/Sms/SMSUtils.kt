package com.tech4bytes.mbrosv3.Sms

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe


class SMSUtils {

    companion object {
        fun sendSMS(context: Context, smsText: String, smsNumber: String) {
            LogMe.log("Sending SMS: $smsNumber - $smsText")
            val sms: SmsManager = SmsManager.getDefault()
            val SENT = "SMS_SENT"
            val DELIVERED = "SMS_DELIVERED"

            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_ONE_SHOT

            val sentPI = PendingIntent.getBroadcast(AppContexts.get(), 0, Intent(SENT), flag)
            val deliveredPI = PendingIntent.getBroadcast(AppContexts.get(), 0, Intent(DELIVERED), flag)

            val broadCastReceiver = object : BroadcastReceiver() {
                override fun onReceive(contxt: Context?, intent: Intent?) {
                    when (resultCode) {
                        AppCompatActivity.RESULT_OK -> Toast.makeText(
                            AppContexts.get(), "SMS sent",
                            Toast.LENGTH_SHORT
                        ).show()
                        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(
                            AppContexts.get(), "Generic failure",
                            Toast.LENGTH_SHORT
                        ).show()
                        SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(
                            AppContexts.get(), "No service",
                            Toast.LENGTH_SHORT
                        ).show()
                        SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(
                            AppContexts.get(), "Null PDU",
                            Toast.LENGTH_SHORT
                        ).show()
                        SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(
                            AppContexts.get(), "Radio off",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            val broadCastReceiverDelivered = object : BroadcastReceiver() {
                override fun onReceive(arg0: Context?, arg1: Intent?) {
                    when (resultCode) {
                        AppCompatActivity.RESULT_OK -> Toast.makeText(
                            AppContexts.get(), "SMS delivered",
                            Toast.LENGTH_SHORT
                        ).show()
                        AppCompatActivity.RESULT_CANCELED -> Toast.makeText(
                            AppContexts.get(), "SMS not delivered",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            context.registerReceiver(broadCastReceiver, IntentFilter(SENT))
            context.registerReceiver(broadCastReceiverDelivered, IntentFilter(DELIVERED))
            LogMe.log(smsText)
            sms.sendTextMessage(smsNumber, null, smsText, sentPI, deliveredPI)
        }
    }
}