package com.prasunmondal.postjsontosheets.clients.commons

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import javax.net.ssl.HttpsURLConnection

class ExecutePostCalls(
    private val scriptUrl: URL,
    private val postDataParams: JSONObject,
    private val onCompletion: Consumer<String>?,
) : AsyncTask<String?, Void?, String>() {
    override fun onPreExecute() {}
    override fun doInBackground(vararg p0: String?): String {
        return try {
            Log.e("DBCall:: Outbound", "scriptURL: $scriptUrl - $postDataParams")
            val conn = scriptUrl.openConnection() as HttpURLConnection
            conn.readTimeout = 150000
            conn.connectTimeout = 150000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            val os = conn.outputStream
            val writer = BufferedWriter(
                OutputStreamWriter(os, StandardCharsets.UTF_8)
            )
            writer.write(getPostDataString(postDataParams))
            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                    break
                }
                bufferedReader.close()
                sb.toString()
            } else {
                "false : $responseCode"
            }
        } catch (e: Exception) {
            "Exception: " + e.message
            throw e
        }
    }

    public override fun onPostExecute(result: String) {
        Log.e("DBCall:: Inbound", result)
        if (onCompletion == null) return
        onCompletion.accept(result)
    }

    @Throws(Exception::class)
    private fun getPostDataString(params: JSONObject): String {
        val result = StringBuilder()
        var first = true
        val itr = params.keys()
        while (itr.hasNext()) {
            val key = itr.next()
            val value = params[key]
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }
}