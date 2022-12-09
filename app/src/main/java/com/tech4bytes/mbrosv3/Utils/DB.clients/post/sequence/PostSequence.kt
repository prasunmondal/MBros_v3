package com.prasunmondal.postjsontosheets.clients.post.raw

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import com.tech4bytes.extrack.DB.clients.ListUtils
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

class PostSequence() : APICalls, PostSequenceFlow, PostSequenceFlow.ScriptIdBuilder,
    PostSequenceFlow.SheetIdBuilder,
    PostSequenceFlow.TabNameBuilder,
    PostSequenceFlow.DataObjectBuilder,
    PostSequenceFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private var dataSequence: MutableList<String> = mutableListOf()

    var onCompletion: Consumer<PostSequenceResponse>? = null

    override fun scriptId(scriptURL: String): PostSequenceFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): PostSequenceFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): PostSequenceFlow.DataObjectBuilder {
        this.tabName = tabName
        return this
    }

    override fun dataObject(dataSequence: String): PostSequenceFlow.FinalRequestBuilder {
        this.dataSequence.add(dataSequence)
        return this
    }

    override fun dataObject(dataSequence: List<String>): PostSequenceFlow.FinalRequestBuilder {
        this.dataSequence = dataSequence as MutableList<String>
        return this
    }

    override fun postCompletion(onCompletion: Consumer<PostSequenceResponse>?): PostSequenceFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun build(): PostSequence {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    override fun execute(): PostSequenceResponse {
        return insertObjectRaw()
    }

    private fun insertObjectRaw(): PostSequenceResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "INSERT_DATA_SEQUENCE")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("dataValue", "[${ListUtils.getCSV(this.dataSequence)}]")

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return PostSequenceResponse(response).getObject()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        var responseObj = PostSequenceResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): PostSequenceFlow.ScriptIdBuilder {
            return PostSequence()
        }
    }
}