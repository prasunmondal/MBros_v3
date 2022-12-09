package com.prasunmondal.postjsontosheets.clients.get

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

class Get() : APICalls, GetFlow, GetFlow.ScriptIdBuilder,
    GetFlow.SheetIdBuilder,
    GetFlow.TabNameBuilder,
    GetFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private var onCompletion: Consumer<GetResponse>? = null
    private var conditionAndColumn = ""
    private var conditionAndValue = ""
    private var conditionOrColumn = ""
    private var conditionOrValue = ""

    override fun scriptId(scriptURL: String): GetFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): GetFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): GetFlow.FinalRequestBuilder {
        this.tabName = tabName
        return this
    }

    override fun postCompletion(onCompletion: Consumer<GetResponse>?): GetFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun conditionAnd(
        conditionColumn: String,
        conditionValue: String
    ): GetFlow.FinalRequestBuilder {
        if (conditionColumn.isEmpty() || conditionValue.isEmpty())
            return this
        this.conditionOrColumn = ""
        this.conditionOrValue = ""
        if (this.conditionAndColumn.isNotEmpty()) {
            this.conditionAndColumn += ","
            this.conditionAndValue += ","
        }
        this.conditionAndColumn += conditionColumn
        this.conditionAndValue += conditionValue
        return this
    }

    override fun conditionOr(
        conditionColumn: String,
        conditionValue: String
    ): GetFlow.FinalRequestBuilder {
        if (conditionColumn.isEmpty() || conditionValue.isEmpty())
            return this
        this.conditionAndColumn = ""
        this.conditionAndValue = ""
        if (this.conditionOrColumn.isNotEmpty()) {
            this.conditionOrColumn += ","
            this.conditionOrValue += ","
        }
        this.conditionOrColumn += conditionColumn
        this.conditionOrValue += conditionValue
        return this
    }

    override fun build(): Get {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    override fun execute(): GetResponse {
        if (conditionOrColumn.isEmpty() && conditionAndColumn.isEmpty())
            return fetchAll()
        else if (conditionAndColumn.isNotEmpty())
            return fetchConditionAnd()
        else
            return fetchConditionOr()
    }

    private fun fetchAll(): GetResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_ALL")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return GetResponse(response).getObject()
    }

    private fun fetchConditionAnd(): GetResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_BY_CONDITION_AND")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("dataColumn", this.conditionAndColumn)
        postDataParams.put("dataValue", this.conditionAndValue)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return GetResponse(response).getObject()
    }

    private fun fetchConditionOr(): GetResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "FETCH_BY_CONDITION_OR")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("dataColumn", this.conditionOrColumn)
        postDataParams.put("dataValue", this.conditionOrValue)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return GetResponse(response).getObject()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        var responseObj = GetResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): GetFlow.ScriptIdBuilder {
            return Get()
        }
    }
}