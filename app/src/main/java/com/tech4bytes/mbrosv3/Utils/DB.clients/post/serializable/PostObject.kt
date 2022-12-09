package com.prasunmondal.postjsontosheets.clients.post.serializable

import com.google.gson.Gson
import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

class PostObject() : APICalls, PostObjectFlow, PostObjectFlow.ScriptIdBuilder,
    PostObjectFlow.SheetIdBuilder,
    PostObjectFlow.TabNameBuilder,
    PostObjectFlow.DataObjectBuilder,
    PostObjectFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private lateinit var dataObject: Any
    private var uniqueColumn = "";

    var onCompletion: Consumer<PostObjectResponse>? = null

    override fun scriptId(scriptURL: String): PostObjectFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): PostObjectFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): PostObjectFlow.DataObjectBuilder {
        this.tabName = tabName
        return this
    }

    override fun dataObject(dataObject: Any): PostObjectFlow.FinalRequestBuilder {
        this.dataObject = dataObject
        return this
    }

    override fun postCompletion(onCompletion: Consumer<PostObjectResponse>?): PostObjectFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun uniqueColumn(uniqueColumn: String): PostObjectFlow.FinalRequestBuilder {
        if (uniqueColumn == "")
            return this
        if (this.uniqueColumn != "")
            this.uniqueColumn += ","
        this.uniqueColumn += uniqueColumn
        return this
    }

    override fun build(): PostObject {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    override fun execute(): PostObjectResponse {
        if (this.uniqueColumn == "")
            return insertData()
        return insertUniqueData()
    }

    private fun insertData(): PostObjectResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "INSERT_OBJECT")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("objectData", Gson().toJson(this.dataObject))

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return PostObjectResponse(response).getObject()
    }

    private fun insertUniqueData(): PostObjectResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "INSERT_OBJECT_UNIQUE")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("objectData", Gson().toJson(this.dataObject))
        postDataParams.put("uniqueCol", this.uniqueColumn)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return PostObjectResponse(response).getObject()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        var responseObj = PostObjectResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): PostObjectFlow.ScriptIdBuilder {
            return PostObject()
        }
    }
}