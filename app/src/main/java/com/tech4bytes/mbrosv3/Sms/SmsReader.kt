package com.tech4bytes.mbrosv3.Sms

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.widget.Toast
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.*
import java.util.stream.Collectors


class SmsReader {

    companion object {
        fun getAllSms(context: Context, fromNumbers: Array<String>): MutableList<SMSModel> {

            SMSPermissions.askPermission(context, android.Manifest.permission.SEND_SMS)
            SMSPermissions.askPermission(context, android.Manifest.permission.READ_SMS)
            val smsList = mutableListOf<SMSModel>()
            val cr: ContentResolver = context.contentResolver

//            val projection = arrayOf("_id", "address", "person", "body", "date", "type")
            val c: Cursor? = cr.query(Telephony.Sms.CONTENT_URI, null,
                "(${getFilterQuery(fromNumbers)})",
                null,
                "date desc")

            var totalSMS = 0
            if (c != null) {
                totalSMS = c.count
                if (c.moveToFirst()) {
                    for (j in 0 until totalSMS) {
                        val smsDate: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        val number: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        val body: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                        val dateFormat = Date(java.lang.Long.valueOf(smsDate))
                        var type: String
                        when (c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).toInt()) {
                            Telephony.Sms.MESSAGE_TYPE_INBOX -> type = "inbox"
                            Telephony.Sms.MESSAGE_TYPE_SENT -> type = "sent"
                            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> type = "outbox"
                            else -> {}
                        }
                        c.moveToNext()
                        smsList.add(SMSModel(number, dateFormat.toString(), body))
                    }
                }
                c.close()
            } else {
                Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show()
            }
            return smsList
        }

        private fun getFilterQuery(fromNumbers: Array<String>): String {
            return fromNumbers.joinToString(
                separator = "' OR  address= '",
                prefix = "address= '",
                postfix = "'"
            )
        }

        fun getSMSStartingWith(smsList: MutableList<SMSModel>, str: String): MutableList<SMSModel> {
            return smsList.stream().filter { p -> p.body.contains(str) }.collect(Collectors.toList())
        }

        fun getSMSFilterQueryForDateFiltering(context: Context): String {
            var noOfDays = NumberUtils.getIntOrZero(AppConstants.get(AppConstants.SMS_ORDERING_SHOW_SMS_FOR_N_DAYS))

            if(noOfDays == 0) {
                // in case of misconfiguration, take 30 as default
                noOfDays = 30
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Error found in misconfig of ${AppConstants.SMS_ORDERING_SHOW_SMS_FOR_N_DAYS.name}. Using fallback value of 30 days", Toast.LENGTH_LONG).show()
                }
            }

            val offset = 1000L * 60 * 60 * 24 * noOfDays
            val currentDate = System.currentTimeMillis()
            val selectedDate = Date(currentDate - offset)
            return "date>=" + selectedDate.time
        }
    }



}