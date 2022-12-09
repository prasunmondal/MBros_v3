package com.prasunmondal.postjsontosheets.clients.get

import com.prasunmondal.postjsontosheets.clients.commons.APIResponse

class IsPresentConditionalAndResponse : APIResponse {

    constructor(responsePayload: String) {
        this.responsePayload = responsePayload
    }

    fun getObject(): IsPresentConditionalAndResponse {
        return this
    }
}