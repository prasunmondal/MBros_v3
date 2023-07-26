package com.tech4bytes.mbrosv3.Sms

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.widget.Toast
import java.util.Date


class SmsReader {

    companion object {
        fun getAllSms(context: Context) {
            val cr: ContentResolver = context.contentResolver
            val c: Cursor? = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null)
            var totalSMS = 0
            if (c != null) {
                totalSMS = c.count
                if (c.moveToFirst()) {
                    for (j in 0 until 10) {
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
                        Toast.makeText(context, body, Toast.LENGTH_SHORT).show()
                    }
                }
                c.close()
            } else {
                Toast.makeText(context, "No message to show!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}