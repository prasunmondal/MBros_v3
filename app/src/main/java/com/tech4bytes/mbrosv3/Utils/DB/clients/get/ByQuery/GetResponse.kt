package com.tech4bytes.mbrosv3.Utils.DB.clients.get.ByQuery

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.commons.APIResponse
import com.prasunmondal.postjsontosheets.clients.commons.JSONUtils
import com.prasunmondal.postjsontosheets.clients.commons.JsonTags

class GetByQueryResponse : APIResponse {

    constructor(responsePayload: String) {
        this.responsePayload = responsePayload
    }

    fun getObject(): GetByQueryResponse {
        return this
    }

    fun <T> getParsedList(): ArrayList<*> {
        var jsonString = JSONUtils.jsonStringCleanUp(this.getRawResponse())
        Log.e("parsing: ", jsonString!!)
        var arrayLabel = "records"
        val parser = JsonParser()
        val jsonObject = parser.parse(jsonString).asJsonObject
        var jsonarray: JsonArray? = null
        try {
            jsonarray = jsonObject.getAsJsonArray(arrayLabel)
        } catch (e: Exception) {
            Log.e("parseJSONObject", "Error while parsing")
        }
        val result: ArrayList<*> = GsonBuilder().create().fromJson(
            jsonarray.toString(),
            object : TypeToken<ArrayList<T>>() {}.type
        )
        return result
    }

    fun numberOfRecordsDeleted(): Int {
        return getJsonObject()!!.get(JsonTags.RESPONSE_ROWS_AFFECTED).asInt
    }
}