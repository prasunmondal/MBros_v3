package com.tech4bytes.mbrosv3.Utils.DB.clients.get.ByQuery

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

@Suppress("DEPRECATION")
class GetByQuery : APICalls, GetByQueryFlow, GetByQueryFlow.ScriptIdBuilder,
    GetByQueryFlow.SheetIdBuilder,
    GetByQueryFlow.TabNameBuilder,
    GetByQueryFlow.AddQueryBuilder,
    GetByQueryFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private var onCompletion: Consumer<GetByQueryResponse>? = null
    private var query = ""

    override fun scriptId(scriptId: String): GetByQueryFlow.SheetIdBuilder {
        this.scriptURL = scriptId
        return this
    }

    override fun sheetId(sheetId: String): GetByQueryFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): GetByQueryFlow.AddQueryBuilder {
        this.tabName = tabName
        return this
    }

    override fun query(query: String): GetByQueryFlow.FinalRequestBuilder {
        this.query = query
        return this
    }

    override fun postCompletion(onCompletion: Consumer<GetByQueryResponse>?): GetByQueryFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    private fun fetchByQuery(): GetResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_BY_QUERY")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("query", this.query)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        val response = c.execute().get()
        return GetResponse(response).getObject()
    }

    override fun build(): GetByQuery {
        return this
    }

    override fun execute(): GetResponse {
        return fetchByQuery()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        val responseObj = GetByQueryResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): GetByQueryFlow.ScriptIdBuilder {
            return GetByQuery()
        }
    }
}