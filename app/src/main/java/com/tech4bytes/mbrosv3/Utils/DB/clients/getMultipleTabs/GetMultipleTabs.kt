package com.tech4bytes.mbrosv3.Utils.DB.clients.getMultipleTabs

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import org.json.JSONObject
import java.io.Serializable
import java.net.URL
import java.util.function.Consumer

class GetMultipleTabs : APICalls, GetMultipleTabsFlow, GetMultipleTabsFlow.ScriptIdBuilder,
    GetMultipleTabsFlow.SheetIdBuilder,
    GetMultipleTabsFlow.TabNameBuilder,
    GetMultipleTabsFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var classesToFetch: List<Tech4BytesSerializable<out Serializable>>
    private var onCompletion: Consumer<GetMultipleTabsResponse>? = null

    override fun scriptId(scriptURL: String): GetMultipleTabsFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): GetMultipleTabsFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun classesToFetch(classesToFetch: List<Tech4BytesSerializable<out Serializable>>): GetMultipleTabsFlow.FinalRequestBuilder {
        this.classesToFetch = classesToFetch
        return this
    }

    override fun postCompletion(onCompletion: Consumer<GetMultipleTabsResponse>?): GetMultipleTabsFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun build(): GetMultipleTabs {
        return this
    }

    override fun execute(): GetMultipleTabsResponse {
        return fetchAllMultipleTabs()
    }

    private fun fetchAllMultipleTabs(): GetMultipleTabsResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_ALL_MULTIPLE_TABS")
        postDataParams.put("sheetId", this.sheetId)
        // tab names are separated by commas <Ex: sheet1, sheet2, sheet3>
        postDataParams.put("tabName", getTabsFromClassesToFetch())

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        val resultsMap = GetMultipleTabsResponse(response).getParsedList()
        resultsMap.forEach { receivedTabName ->
            this.classesToFetch.forEach {
                if (receivedTabName.key.equals(it.tabname)) {
                    it.parseAndSaveToCache(GetResponse(resultsMap[receivedTabName.key]!!))
                }
            }
        }
        return GetMultipleTabsResponse(response).getObject()
    }

    private fun getTabsFromClassesToFetch(): String {
        val tabList = mutableListOf<String>()
        this.classesToFetch.forEach {
            tabList.add(it.tabname)
        }
        return tabList.joinToString(", ").trim()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        val responseObj = GetMultipleTabsResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): GetMultipleTabsFlow.ScriptIdBuilder {
            return GetMultipleTabs()
        }
    }
}