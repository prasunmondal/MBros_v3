package com.tech4bytes.mbrosv3.OneShot.Delivery

import com.tech4bytes.extrack.centralCache.CentralCache
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummary
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts

class CompanyLoadMap : java.io.Serializable {
    var companyName = ""
    var branch = ""
    var area = ""
    var moneyAccount = ""

    companion object {

        private val cacheKey = "CompanyLoadMap"
        fun get(useCache: Boolean = true): List<CompanyLoadMap> {
            var cacheResults = CentralCache.get<List<CompanyLoadMap>>(AppContexts.get(), cacheKey, useCache)

            if (cacheResults == null) {
                cacheResults = prepareData()
                CentralCache.put(cacheKey, cacheResults)
            }
            return cacheResults
        }

        fun prepareData(): List<CompanyLoadMap> {
            val list = mutableListOf<CompanyLoadMap>()
            DaySummary.get().forEach {
                val t = CompanyLoadMap()
                t.companyName = it.loadingCompany
                t.branch = it.loadingBranch
                t.area = it.loadingArea
                t.moneyAccount = it.loadingComapnyAccount
                list.add(t)
            }
            return list
        }
    }
}