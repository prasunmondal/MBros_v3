package com.prasunmondal.postjsontosheets.clients.commons

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.lang.reflect.Type

open class APIResponse {
    var responsePayload: String = ""

    fun getRawResponse(): String {
        return this.responsePayload
    }

    fun getResponseCode(): Int {
        return getJsonObject()!!.get(JsonTags.RESPONSE_RESPONSE_CODE).asInt
    }

    fun getOpCode(): Int {
        return getJsonObject()!!.get(JsonTags.RESPONSE_OP_CODE).asInt
    }

    fun getJsonObject(responseString: String = ""): JsonObject? {
        val parsingString = if(responseString.isNotEmpty()) responseString else responsePayload
        val parser = JsonParser()
        return parser.parse(parsingString).asJsonObject
    }

    fun <T> parseToObject(jsonString: String?, type: Type, doesContainSheetName: Boolean = false): ArrayList<T> {
        Log.e("parsing to object ", jsonString!!)
        var arrayLabel = JsonTags.RESPONSE_DATA_CODE
        var jsonarray: JsonArray? = null
        try {
            jsonarray = getJsonObject()!!.getAsJsonArray(arrayLabel)
            jsonarray.forEach {
                LogMe.log(it.asString)
            }
        } catch (e: Exception) {
            Log.e("parseJSONObject", e.stackTraceToString())
            Log.e("parseJSONObject", "Error while parsing")
        }
        return try {
            val result: ArrayList<T> = GsonBuilder().create().fromJson(jsonarray.toString(), type)
            result
        } catch (npe: NullPointerException) {
            arrayListOf()
        }
    }

    fun getExceptionMessage(): String {
        return ""
    }

    fun getExceptionStackTrace(): String {
        return ""
    }

    fun isOperationLocked(): Boolean {
        return getJsonObject()!!.get(JsonTags.RESPONSE_IS_LOCKED_OPERATION).asBoolean
    }
}