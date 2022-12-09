package com.prasunmondal.postjsontosheets.clients.delete

import java.util.function.Consumer

interface DeleteFlow {

    interface ScriptIdBuilder {
        fun scriptId(scriptId: String): SheetIdBuilder
    }

    interface SheetIdBuilder {
        fun sheetId(sheetId: String): TabNameBuilder
    }

    interface TabNameBuilder {
        fun tabName(tabName: String): FinalRequestBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): Delete
        fun postCompletion(onCompletion: Consumer<DeleteResponse>?): FinalRequestBuilder
        fun conditionAnd(conditionKey: String, conditionValue: String): FinalRequestBuilder
        fun conditionOr(conditionKey: String, conditionValue: String): FinalRequestBuilder
        fun deleteAll(): FinalRequestBuilder
    }

    fun execute(): DeleteResponse
}