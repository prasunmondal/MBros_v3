package com.tech4bytes.mbrosv3.Payments.Staged

import com.google.gson.reflect.TypeToken
import com.prasunmondal.postjsontosheets.clients.get.Get
import com.prasunmondal.postjsontosheets.clients.get.GetResponse
import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.Payments.PaymentsType
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class StagedPaymentUtils {
    
    companion object {

        fun getStagedPayments(name: String, useCache: Boolean = true): StagedPaymentsModel {
            var list = getAllStagedPayments(useCache).filter { it.name == name }
            var sumPaid = 0
            var allNotes = ""
            list.forEach {
                sumPaid += NumberUtils.getIntOrZero(it.paidAmount)
                allNotes += "${it.name} paid Rs ${it.paidAmount} (${it.paymentMode}) recorded on ${it.datetime}."
                if(it.notes.trim().isEmpty()) {
                    allNotes += "[Note: ${it.notes}]."
                }
            }
            return StagedPaymentsModel(
                id = "",
                datetime = "",
                prevBalance = "",
                name = name,
                transactionType = PaymentsType.CREDIT,
                paidAmount = sumPaid.toString(),
                newBalance = "",
                paymentMode = "",
                notes = allNotes
            )
        }

        fun getAllStagedPayments(useCache: Boolean = true): ArrayList<StagedPaymentsModel>{
            LogMe.log("Getting delivery records")
            val cacheResults = CentralCache.get<ArrayList<StagedPaymentsModel>>(AppContexts.get(), StagedPaymentsModel.tabname, useCache)
            LogMe.log("Getting delivery records: Cache Hit: " + (cacheResults != null))
            return if (cacheResults != null) {
                cacheResults
            } else {
                val resultFromServer = getFromServer()
                CentralCache.put(StagedPaymentsModel.tabname, resultFromServer)
                resultFromServer
            }
        }

        private fun getFromServer(): ArrayList<StagedPaymentsModel> {
            val result: GetResponse = Get.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(StagedPaymentsModel.sheet)
                .tabName(StagedPaymentsModel.tabname)
                .build().execute()

            return result.parseToObject(
                result.getRawResponse(),
                object : TypeToken<ArrayList<StagedPaymentsModel>?>() {}.type
            )
        }

        fun save(stagedObj: StagedPaymentsModel) {
            saveToLocal(stagedObj)
            saveToServer(stagedObj)
        }

        fun saveToLocal(stagedObj: StagedPaymentsModel) {
            CentralCache.put(StagedPaymentsModel.tabname, stagedObj)
        }

        fun saveToServer(stagedObj: StagedPaymentsModel) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(StagedPaymentsModel.sheet)
                .tabName(StagedPaymentsModel.tabname)
                .dataObject(stagedObj as Any)
                .build().execute()
        }
    }
}