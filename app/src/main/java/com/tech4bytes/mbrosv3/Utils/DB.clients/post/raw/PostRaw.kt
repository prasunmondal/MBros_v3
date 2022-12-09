package com.prasunmondal.postjsontosheets.clients.post.raw

import com.google.gson.Gson
import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

class PostRaw() : APICalls, PostRawFlow, PostRawFlow.ScriptIdBuilder,
    PostRawFlow.SheetIdBuilder,
    PostRawFlow.TabNameBuilder,
    PostRawFlow.DataObjectBuilder,
    PostRawFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private lateinit var dataObject: Object

    var onCompletion: Consumer<PostRawResponse>? = null

    override fun scriptId(scriptURL: String): PostRawFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): PostRawFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): PostRawFlow.DataObjectBuilder {
        this.tabName = tabName
        return this
    }

    override fun dataObject(dataObject: Object): PostRawFlow.FinalRequestBuilder {
        this.dataObject = dataObject
        return this
    }

    override fun postCompletion(onCompletion: Consumer<PostRawResponse>?): PostRawFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun build(): PostRaw {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    override fun execute(): PostRawResponse {
        return insertObjectRaw()
    }

    private fun insertObjectRaw(): PostRawResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "INSERT_RAW_OBJECT")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("objectData", Gson().toJson(this.dataObject))

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return PostRawResponse(response).getObject()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        var responseObj = PostRawResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): PostRawFlow.ScriptIdBuilder {
            return PostRaw()
        }
    }
}