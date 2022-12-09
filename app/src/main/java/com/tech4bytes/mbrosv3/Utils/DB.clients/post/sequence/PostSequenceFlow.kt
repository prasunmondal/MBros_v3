package com.prasunmondal.postjsontosheets.clients.post.raw

import java.util.function.Consumer

interface PostSequenceFlow {

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
        fun dataObject(dataObject: String): FinalRequestBuilder
        fun dataObject(dataObject: List<String>): FinalRequestBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): PostSequence
        fun postCompletion(onCompletion: Consumer<PostSequenceResponse>?): FinalRequestBuilder
    }

    fun execute(): PostSequenceResponse
}