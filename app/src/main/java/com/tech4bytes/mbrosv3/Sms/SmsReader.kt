package com.tech4bytes.mbrosv3.Sms

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.widget.Toast
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders
import java.util.Date
import java.util.stream.Collectors


class SmsReader {

    companion object {
        fun getAllSms(context: Context): MutableList<SMSModel> {
            val smsList = mutableListOf<SMSModel>()

            val cr: ContentResolver = context.contentResolver
            val c: Cursor? = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null)
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

        fun getSMSFromNumber(smsList: MutableList<SMSModel>, number: String): MutableList<SMSModel> {
            return smsList.stream().filter{ p -> p.number.contains(number) }.collect(Collectors.toList())
        }

        fun getSMSStartingWith(smsList: MutableList<SMSModel>, str: String): MutableList<SMSModel> {
            return smsList.stream().filter{ p -> p.body.contains(str) }.collect(Collectors.toList())
        }
    }


}