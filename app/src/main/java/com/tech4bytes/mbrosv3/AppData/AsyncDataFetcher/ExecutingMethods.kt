package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataModel
import kotlin.reflect.KFunction

class ExecutingMethods : java.io.Serializable {
    private var listOfExecutingMethods: MutableMap<KFunction<Any>, ExecutingMethodModel> = mutableMapOf()

    fun add(executingMethod: KFunction<Any>, func: () -> List<Any>, useCache: Boolean = true) {
        listOfExecutingMethods[executingMethod] =
            ExecutingMethodModel(
                func,
                DataFetchingInfo.getDescription(executingMethod),
                useCache
            )
    }

    fun get(): MutableMap<KFunction<Any>, ExecutingMethodModel> {
        return listOfExecutingMethods
    }
}
