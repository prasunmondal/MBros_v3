package com.tech4bytes.mbrosv3.Utils.FileDownload

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import java.io.File

open class DownloadUtil(private val context: Context) {

    private val FILE_BASE_PATH = "file://"
    private val MIME_TYPE = "application/vnd.android.package-archive"

    fun enqueueDownload(
        context: Context,
        url: String,
        destination: String,
        downloadTitle: String,
        downloadDescription: String,
        onComplete: () -> Unit?
    ) {
        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists()) file.delete()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        request.setMimeType(MIME_TYPE)
        request.setTitle(downloadTitle)
        request.setDescription(downloadDescription)
        request.setDestinationUri(uri)
        if (onComplete != null)
            onCompletion(onComplete)
        downloadManager.enqueue(request)
    }

    private fun onCompletion(onComplete: () -> Unit?) {
        // on download complete...
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                onComplete.invoke()
                context.unregisterReceiver(this)
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}