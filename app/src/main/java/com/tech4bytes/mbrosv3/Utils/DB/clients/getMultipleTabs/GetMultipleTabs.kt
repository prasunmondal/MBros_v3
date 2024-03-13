package com.tech4bytes.mbrosv3.Utils.DB.clients.getMultipleTabs

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
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

    override fun execute(): GetMultipleTabsResponse {
        return fetchAllMultipleTabs()
    }

    private fun fetchAllMultipleTabs(): GetMultipleTabsResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_ALL_MULTIPLE_TABS")
        postDataParams.put("sheetId", this.sheetId)
        // tab names are separated by commas <Ex: sheet1, sheet2, sheet3>
        postDataParams.put("tabName", this.tabName)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
//        var response = "{\"records\":{\"dataFreshness\":[{\"metadata\":\"7055b8269e9ee540\",\"deliverOrders\":\"ddd\",\"fuel\":1699020149058,\"metadataForCalc\":\"7055b8269e9ee540\"}],\"smsModel\":[{\"platform\":\"SMS\",\"sendTo\":7001553445,\"communicationType\":\"DELIVERY_SMS\",\"inputData\":\"Prabir\",\"dataTemplate\":\"Date: <date>\\nDelivery Details: <pc>pc / <kg>kg\\n\\n- Mondal Bros.\",\"enablement_template\":\"<pc>,<kg>\",\"isEnabled\":true},{\"platform\":\"SMS\",\"sendTo\":7001553445,\"communicationType\":\"DELIVERY_SMS\",\"inputData\":\"Mathura\",\"dataTemplate\":\"Date: <date>\\nCustomer: <name>\\nDelivery Details: <pc>pc / <kg>kg\\n\\n- Mondal Bros.\",\"enablement_template\":\"<pc>,<kg>\",\"isEnabled\":true},{\"platform\":\"SMS\",\"sendTo\":9734075801,\"communicationType\":\"DAY_SUMMARY\",\"inputData\":\"\",\"dataTemplate\":\"Date: <date>\\nLoad: <loadPc>pc / <loadKg>kg\\nShortage: <shortage> kg\\nKM: <km>\\n\\n- Mondal Bros.\",\"enablement_template\":\"\",\"isEnabled\":true},{\"platform\":\"SMS\",\"sendTo\":9679004046,\"communicationType\":\"DAY_SUMMARY\",\"inputData\":\"\",\"dataTemplate\":\"Date: <date>\\nLoad: <loadPc>pc / <loadKg>kg\\nShortage: <shortage> kg\\nKM: <km>\\n\\n- Mondal Bros.\",\"enablement_template\":\"\",\"isEnabled\":true},{\"platform\":\"SMS\",\"sendTo\":9679004046,\"communicationType\":\"LOAD_DETAILS\",\"inputData\":\"Shalimar\",\"dataTemplate\":\"Date: <date>\\nSend Money to Shalimar\\n\\n- Mondal Bros.\",\"enablement_template\":\"\",\"isEnabled\":true},{\"platform\":\"WHATSAPP\",\"sendTo\":918617434349,\"communicationType\":\"LOAD_DETAILS\",\"inputData\":\"Montu\",\"dataTemplate\":\"Date: <date>\\nFrom: <loadCompanyName>\\nLoad Details: <loadPc>pc / <loadKg>kg\\n\\n- Mondal Bros.\",\"enablement_template\":\"\",\"isEnabled\":true},{\"platform\":\"WHATSAPP\",\"sendTo\":919679004046,\"communicationType\":\"KHATA\",\"inputData\":\"\",\"dataTemplate\":\"Send Day Record File\",\"enablement_template\":\"\",\"isEnabled\":false}]}}"
//        this.sheetClassMap[]
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
}