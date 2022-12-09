package com.prasunmondal.postjsontosheets.clients.delete

import com.prasunmondal.postjsontosheets.clients.commons.APIResponse
import com.prasunmondal.postjsontosheets.clients.commons.JsonTags

class DeleteResponse : APIResponse {

    constructor(responsePayload: String) {
//        if(responsePayload.contains("Unable to resolve host \"script.google.com\": No address associated with hostname")) {
//            throw ConnectionErrorException()
//        }
        this.responsePayload = responsePayload
    }

    fun getObject(): DeleteResponse {
        return this
    }

    fun numberOfRecordsFetched(): Int {
        return getJsonObject()!!.get(JsonTags.RESPONSE_ROWS_AFFECTED).asInt
    }
}