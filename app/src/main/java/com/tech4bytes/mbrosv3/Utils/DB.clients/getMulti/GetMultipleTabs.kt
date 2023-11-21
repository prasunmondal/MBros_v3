package com.prasunmondal.postjsontosheets.clients.get

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.APIResponse
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer
import kotlin.reflect.KFunction

class GetMultipleTabs: APICalls, GetMultipleTabsFlow, GetMultipleTabsFlow.ScriptIdBuilder, GetMultipleTabsFlow.SheetClassMapBuilder,
    GetMultipleTabsFlow.SheetIdBuilder,
    GetMultipleTabsFlow.TabNameBuilder,
    GetMultipleTabsFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private lateinit var sheetClassMap: MutableMap<String, KFunction<Any>>
    private var onCompletion: Consumer<GetMultipleTabsResponse>? = null

    private fun <T> parseToObjectList(result: GetMultipleTabsResponse): List<T> {
        return result.parseToObject(
            result.getRawResponse(),
            object : TypeToken<ArrayList<T>?>() {}.type)
    }

    fun <T> parseAndSaveToLocal(cacheKey: String, result: GetMultipleTabsResponse): List<T> {
        val parsedObj = parseToObjectList<T>(result)
        CentralCache.put(cacheKey, parsedObj)
        return parsedObj
    }

    override fun postCompletion(onCompletion: Consumer<GetMultipleTabsResponse>?): GetMultipleTabsFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun SheetClassMapBuilder(sheetClassMap: MutableMap<String, KFunction<Any>>): GetMultipleTabsFlow.FinalRequestBuilder {
        this.sheetClassMap = sheetClassMap
        return this
    }

    override fun build(): GetMultipleTabs {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    fun <T> execute(cacheKey: String): GetMultipleTabsResponse {
        val result =
            fetchAllMultipleTabs()
        parseAndSaveToLocal<T>(cacheKey, result)
        return result
    }

    private fun fetchAllMultipleTabs(): GetMultipleTabsResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_ALL_MULTIPLE_TABS")
        postDataParams.put("sheetId", this.sheetId)
        // tab names are separated by commas <Ex: sheet1, sheet2, sheet3>
        postDataParams.put("tabName", this.tabName)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        val response = c.execute().get()
        Log.e("DBCall:: Inbound", response)
        val resultsMap = GetMultipleTabsResponse(response).getParsedList()
        resultsMap.forEach {
            LogMe.log("Converting ${it.key} to object: ${resultsMap[it.key]}")
            (sheetClassMap[it.key] as ((String, GetResponse) -> Unit)).invoke(it.key, GetResponse(resultsMap[it.key]!!))
        }
        return GetMultipleTabsResponse(response).getObject()
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

    override fun execute(): GetMultipleTabsResponse {
        val result =
            fetchAllMultipleTabs()
        return result
    }

    override fun scriptId(scriptURL: String): GetMultipleTabsFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): GetMultipleTabsFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): GetMultipleTabsFlow.SheetClassMapBuilder {
        this.tabName = tabName
        return this
    }
}