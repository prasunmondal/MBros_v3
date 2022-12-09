package com.prasunmondal.postjsontosheets.clients.post.raw

import java.util.function.Consumer

interface PostRawFlow {

    interface ScriptIdBuilder {
        fun scriptId(scriptId: String): SheetIdBuilder
    }

    interface SheetIdBuilder {
        fun sheetId(sheetId: String): TabNameBuilder
    }

    interface TabNameBuilder {
        fun tabName(tabName: String): DataObjectBuilder
    }

    interface DataObjectBuilder {
        fun dataObject(dataObject: Object): FinalRequestBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): PostRaw
        fun postCompletion(onCompletion: Consumer<PostRawResponse>?): FinalRequestBuilder
    }

    fun execute(): PostRawResponse
}