package com.prasunmondal.postjsontosheets.clients.get

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

class IsPresentConditionalAnd() : APICalls, IsPresentConditionalAndFlow,
    IsPresentConditionalAndFlow.ScriptIdBuilder,
    IsPresentConditionalAndFlow.SheetIdBuilder,
    IsPresentConditionalAndFlow.TabNameBuilder,
    IsPresentConditionalAndFlow.FinalRequestBuilder,
    IsPresentConditionalAndFlow.KeysBuilder,
    IsPresentConditionalAndFlow.ValuesBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private lateinit var keys: String
    private lateinit var values: String

    var onCompletion: Consumer<IsPresentConditionalAndResponse>? = null

    override fun scriptId(scriptURL: String): IsPresentConditionalAndFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): IsPresentConditionalAndFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): IsPresentConditionalAndFlow.KeysBuilder {
        this.tabName = tabName
        return this
    }

    override fun keys(keys: String): IsPresentConditionalAndFlow.ValuesBuilder {
        this.keys = keys
        return this
    }

    override fun values(values: String): IsPresentConditionalAndFlow.FinalRequestBuilder {
        this.values = values
        return this
    }

    override fun postCompletion(onCompletion: Consumer<IsPresentConditionalAndResponse>?): IsPresentConditionalAndFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun build(): IsPresentConditionalAnd {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    override fun execute(): IsPresentConditionalAndResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "IS_PRESENT_CONDITIONAL_AND")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("dataValue", keys)
        postDataParams.put("dataColumn", values)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return IsPresentConditionalAndResponse(response).getObject()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        var responseObj = IsPresentConditionalAndResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): IsPresentConditionalAndFlow.ScriptIdBuilder {
            return IsPresentConditionalAnd()
        }
    }
}