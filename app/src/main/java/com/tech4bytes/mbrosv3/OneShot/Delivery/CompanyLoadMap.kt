package com.tech4bytes.mbrosv3.OneShot.Delivery

import com.prasunmondal.dev.libs.caching.CentralCacheObj
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.Summary.DaySummary.DaySummaryUtils

class CompanyLoadMap : java.io.Serializable {
    var companyName = ""
    var branch = ""
    var area = ""
    var moneyAccount = ""

    companion object {

        private val cacheKey = "CompanyLoadMap"
        fun get(useCache: Boolean = true): List<CompanyLoadMap> {
            var cacheResults = CentralCacheObj.centralCache.get<List<CompanyLoadMap>>(AppContexts.get(), cacheKey, useCache)

            if (cacheResults == null) {
                cacheResults = prepareData()
                CentralCacheObj.centralCache.put(AppContexts.get(), cacheKey, cacheResults)
            }
            return cacheResults
        }

        fun prepareData(): List<CompanyLoadMap> {
            val list = mutableListOf<CompanyLoadMap>()
            DaySummaryUtils.fetchAll().execute().forEach {
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