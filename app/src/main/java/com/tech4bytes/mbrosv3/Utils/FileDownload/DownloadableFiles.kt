package com.tech4bytes.mbrosv3.Utils.FileDownload

import android.content.Context
import android.os.Environment
import com.prasunmondal.dev.libs.contexts.AppContexts
import java.io.File

class DownloadableFiles {
    private var externalFileDir = ""

    private var fileServerURL: String
    private var subDirectory: String
    private lateinit var fileName: String
    private var downloadTitle: String
    private var downloadDescription: String
    private var onComplete: () -> Unit
    private lateinit var context: Context
    private var localURL = ""

    constructor(
        context: Context,
        subDirectory: String,
        fileName: String,
        fileServerURL: String,
        downloadTitle: String,
        downloadDescription: String,
        onComplete: () -> Unit
    ) {
        this.fileServerURL = fileServerURL
        this.subDirectory = subDirectory
        this.fileName = fileName
        this.downloadTitle = downloadTitle
        this.downloadDescription = downloadDescription
        this.onComplete = onComplete
        this.context = context

        this.externalFileDir = AppContexts.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        this.localURL = "$externalFileDir/$subDirectory/$fileName"
    }

    constructor(
        externalFileDir: String,
        subDirectory: String,
        fileName: String,
        fileServerURL: String,
        downloadTitle: String,
        downloadDescription: String,
        onComplete: () -> Unit
    ) {
        this.fileServerURL = fileServerURL
        this.subDirectory = subDirectory
        this.fileName = fileName
        this.downloadTitle = downloadTitle
        this.downloadDescription = downloadDescription
        this.onComplete = onComplete

        this.externalFileDir = externalFileDir
    }

    fun download() {
        DownloadUtil(context).enqueueDownload(
            context, fileServerURL, localURL,
            downloadTitle, downloadDescription,
            onComplete
        )
    }

    fun download(context: Context) {
        DownloadUtil(context).enqueueDownload(
            context, fileServerURL, localURL,
            downloadTitle, downloadDescription,
            onComplete
        )
    }

    fun download(context: Context, onComplete: () -> Unit) {
        DownloadUtil(context).enqueueDownload(
            context, fileServerURL, localURL,
            downloadTitle, downloadDescription,
            onComplete
        )
    }

    fun doesExist(): Boolean {
        val file = File(this.localURL)
        return file.exists()
    }

    fun getServerURL(): String {
        return fileServerURL
    }

    fun getLocalURL(): String {
        return localURL
    }

    fun getFilename(): String {
        return fileName
    }
}