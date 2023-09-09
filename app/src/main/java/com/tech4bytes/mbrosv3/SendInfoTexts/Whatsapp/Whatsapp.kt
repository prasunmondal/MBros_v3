package com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

class Whatsapp {

    companion object {

        /***
         * toNumber
         * Replace with mobile phone number without +Sign or leading zeros, but with country code
         * Suppose your country is India and your phone number is “xxxxxxxxxx”, then you need to send “91xxxxxxxxxx”.
         */
        fun sendMessage(context: Context, toNumber: String, text: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$toNumber&text=$text")
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun sendMessage(context: Context, text: String) {
            try {
                val waIntent = Intent(Intent.ACTION_SEND)
                waIntent.type = "text/plain"
                waIntent.setPackage("com.whatsapp")
                waIntent.putExtra(Intent.EXTRA_TEXT, text)
                context.startActivity(Intent.createChooser(waIntent, "Share with"))
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(context, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}