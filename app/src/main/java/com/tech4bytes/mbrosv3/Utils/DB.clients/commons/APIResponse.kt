package com.prasunmondal.postjsontosheets.clients.commons

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstantModel
import java.lang.NullPointerException
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

    fun getJsonObject(): JsonObject? {
        val parser = JsonParser()
        return parser.parse(responsePayload).asJsonObject
    }

    fun <T> parseToObject(jsonString: String?, type: Type): ArrayList<T> {
        Log.e("parsing to object ", jsonString!!)
        var arrayLabel = JsonTags.RESPONSE_DATA_CODE
        var jsonarray: JsonArray? = null
        var result = arrayListOf<T>()
        try {
            jsonarray = getJsonObject()!!.getAsJsonArray(arrayLabel)
            result = GsonBuilder().create().fromJson(jsonarray.toString(), type)
        } catch (e: NullPointerException) {
            Log.e("parseJSONObject", "No value fetched")
        } catch (e: Exception) {
            Log.e("parseJSONObject", "Error while parsing")
        }

        return result
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