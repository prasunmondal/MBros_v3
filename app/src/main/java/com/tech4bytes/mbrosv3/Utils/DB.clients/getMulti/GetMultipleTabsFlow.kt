package com.prasunmondal.postjsontosheets.clients.get

import java.util.function.Consumer
import kotlin.reflect.KFunction

interface GetMultipleTabsFlow {

    interface ScriptIdBuilder {
        fun scriptId(scriptId: String): SheetIdBuilder
    }

    interface SheetIdBuilder {
        fun sheetId(sheetId: String): TabNameBuilder
    }

    interface TabNameBuilder {
        fun tabName(tabName: String): SheetClassMapBuilder
    }

    interface SheetClassMapBuilder {
        fun SheetClassMapBuilder(sheetClassMap: MutableMap<String, KFunction<Any>>): FinalRequestBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): GetMultipleTabs
        fun postCompletion(onCompletion: Consumer<GetMultipleTabsResponse>?): FinalRequestBuilder
    }

    fun execute(): GetMultipleTabsResponse
}