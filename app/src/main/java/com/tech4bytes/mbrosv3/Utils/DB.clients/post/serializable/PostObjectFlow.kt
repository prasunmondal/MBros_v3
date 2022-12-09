package com.prasunmondal.postjsontosheets.clients.post.serializable

import java.util.function.Consumer

interface PostObjectFlow {

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
        fun dataObject(dataObject: Any): FinalRequestBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): PostObject
        fun postCompletion(onCompletion: Consumer<PostObjectResponse>?): FinalRequestBuilder
        fun uniqueColumn(uniqueColumn: String): FinalRequestBuilder
    }

    fun execute(): PostObjectResponse
}