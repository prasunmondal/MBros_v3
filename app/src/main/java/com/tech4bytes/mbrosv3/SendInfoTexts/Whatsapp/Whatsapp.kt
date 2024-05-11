package com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.tech4bytes.mbrosv3.BuildConfig
import java.io.File
import java.util.Objects


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

        fun sendFileUsingWhatsapp(context: Context, filePath: String, number: String, text: String) {
            val outputPath = File(filePath)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            val fileUri = FileProvider.getUriForFile(Objects.requireNonNull(context),
                BuildConfig.APPLICATION_ID + ".provider", outputPath)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.putExtra("jid", "$number@s.whatsapp.net")
            context.startActivity(Intent.createChooser(shareIntent, "Share Khata"))
        }
    }
}