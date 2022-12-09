package com.prasunmondal.postjsontosheets.clients.delete

import com.prasunmondal.postjsontosheets.clients.commons.APICalls
import com.prasunmondal.postjsontosheets.clients.commons.ExecutePostCalls
import org.json.JSONObject
import java.net.URL
import java.util.function.Consumer

class Delete private constructor() : APICalls, DeleteFlow, DeleteFlow.ScriptIdBuilder,
    DeleteFlow.SheetIdBuilder,
    DeleteFlow.TabNameBuilder,
    DeleteFlow.FinalRequestBuilder {
    private lateinit var scriptURL: String
    private lateinit var sheetId: String
    private lateinit var tabName: String
    private var onCompletion: Consumer<DeleteResponse>? = null
    private var conditionAndColumn = ""
    private var conditionAndValue = ""
    private var conditionOrColumn = ""
    private var conditionOrValue = ""
    private var isDeleteAllOp = false

    override fun scriptId(scriptURL: String): DeleteFlow.SheetIdBuilder {
        this.scriptURL = scriptURL
        return this
    }

    override fun sheetId(sheetId: String): DeleteFlow.TabNameBuilder {
        this.sheetId = sheetId
        return this
    }

    override fun tabName(tabName: String): DeleteFlow.FinalRequestBuilder {
        this.tabName = tabName
        return this
    }

    override fun postCompletion(onCompletion: Consumer<DeleteResponse>?): DeleteFlow.FinalRequestBuilder {
        this.onCompletion = onCompletion
        return this
    }

    override fun conditionAnd(
        conditionColumn: String,
        conditionValue: String
    ): DeleteFlow.FinalRequestBuilder {
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
    ): DeleteFlow.FinalRequestBuilder {
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

    override fun deleteAll(): DeleteFlow.FinalRequestBuilder {
        isDeleteAllOp = true
        return this
    }

    override fun build(): Delete {
        this.scriptURL = scriptURL
        this.sheetId = sheetId
        this.tabName = tabName
        this.onCompletion = onCompletion
        return this
    }

    override fun execute(): DeleteResponse {
        if (isDeleteAllOp)
            return deleteAllExecute()
        if (conditionOrColumn.isEmpty() && conditionAndColumn.isEmpty()) {
            return deleteAllExecute()
            TODO("throw error if nothing is specified")
        } else if (conditionAndColumn.isNotEmpty())
            return deleteConditionAnd()
        else
            return deleteConditionOr()
    }

    private fun deleteAllExecute(): DeleteResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "DELETE_ALL")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return DeleteResponse(response).getObject()
    }

    private fun deleteConditionAnd(): DeleteResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "DELETE_CONDITIONAL_AND")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("dataColumn", this.conditionAndColumn)
        postDataParams.put("dataValue", this.conditionAndValue)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return DeleteResponse(response).getObject()
    }

    private fun deleteConditionOr(): DeleteResponse {
        val scriptUrl = URL(this.scriptURL)
        val postDataParams = JSONObject()
        postDataParams.put("opCode", "DELETE_CONDITIONAL_OR")
        postDataParams.put("sheetId", this.sheetId)
        postDataParams.put("tabName", this.tabName)
        postDataParams.put("dataColumn", this.conditionOrColumn)
        postDataParams.put("dataValue", this.conditionOrValue)

        val c = ExecutePostCalls(scriptUrl, postDataParams) { response -> postExecute(response) }
        var response = c.execute().get()
        return DeleteResponse(response).getObject()
    }

    private fun postExecute(response: String) {
        if (onCompletion == null)
            return
        var responseObj = DeleteResponse(response)
        onCompletion!!.accept(responseObj)
    }

    companion object {
        fun builder(): DeleteFlow.ScriptIdBuilder {
            return Delete()
        }
    }
}