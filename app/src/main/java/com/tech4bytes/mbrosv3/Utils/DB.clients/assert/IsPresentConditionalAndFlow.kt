package com.prasunmondal.postjsontosheets.clients.get

import java.util.function.Consumer

interface IsPresentConditionalAndFlow {

    interface ScriptIdBuilder {
        fun scriptId(scriptId: String): SheetIdBuilder
    }

    interface SheetIdBuilder {
        fun sheetId(sheetId: String): TabNameBuilder
    }

    interface KeysBuilder {
        fun keys(sheetId: String): ValuesBuilder
    }

    interface ValuesBuilder {
        fun values(sheetId: String): FinalRequestBuilder
    }

    interface TabNameBuilder {
        fun tabName(tabName: String): KeysBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): IsPresentConditionalAnd
        fun postCompletion(onCompletion: Consumer<IsPresentConditionalAndResponse>?): FinalRequestBuilder
    }

    fun execute(): IsPresentConditionalAndResponse
}