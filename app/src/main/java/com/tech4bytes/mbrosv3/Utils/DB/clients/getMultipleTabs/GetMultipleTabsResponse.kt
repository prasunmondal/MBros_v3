package com.tech4bytes.mbrosv3.Utils.DB.clients.getMultipleTabs

import com.google.gson.JsonParser
import com.prasunmondal.postjsontosheets.clients.commons.APIResponse
import com.prasunmondal.postjsontosheets.clients.commons.JsonTags
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class GetMultipleTabsResponse : APIResponse {

    constructor(responsePayload: String) {
        this.responsePayload = responsePayload
    }

    fun getObject(): GetMultipleTabsResponse {
        return this
    }

    fun getParsedList(): MutableMap<String, String> {
        LogMe.log(responsePayload)
        val parser = JsonParser()
        var po = parser.parse(responsePayload).asJsonObject
        var t = po.get("records").asJsonObject

        var responseList: MutableMap<String, String> = mutableMapOf()
        t.entrySet()
        t.entrySet().forEach { k ->
            LogMe.log("${k.key} :: ${k.value}")
            responseList[k.key] = "{\"records\":" + k.value + "}"
        }
        return responseList
    }

    fun numberOfRecordsDeleted(): Int {
        return getJsonObject()!!.get(JsonTags.RESPONSE_ROWS_AFFECTED).asInt
    }
}