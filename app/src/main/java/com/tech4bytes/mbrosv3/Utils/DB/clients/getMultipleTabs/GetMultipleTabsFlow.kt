package com.tech4bytes.mbrosv3.Utils.DB.clients.getMultipleTabs

import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import java.io.Serializable
import java.util.function.Consumer

interface GetMultipleTabsFlow {

    interface ScriptIdBuilder {
        fun scriptId(scriptId: String): SheetIdBuilder
    }

    interface SheetIdBuilder {
        fun sheetId(sheetId: String): TabNameBuilder
    }

    interface TabNameBuilder {
        fun classesToFetch(tabName: List<Tech4BytesSerializable<out Serializable>>): FinalRequestBuilder
    }

    interface FinalRequestBuilder {
        // All optional parameters goes here
        fun build(): GetMultipleTabs
        fun postCompletion(onCompletion: Consumer<GetMultipleTabsResponse>?): FinalRequestBuilder
    }

    fun execute(): GetMultipleTabsResponse
}