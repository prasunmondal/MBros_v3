package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import kotlin.reflect.KFunction

class ExecutingMethods: java.io.Serializable {
    var listOfExecutingMethods: MutableMap<KFunction<Any>, String> = mutableMapOf()

    fun add(executingMethod: KFunction<Any>) {
        listOfExecutingMethods[executingMethod] = DataFetchingInfo.getDescription(executingMethod)
    }

    fun get(): MutableMap<KFunction<Any>, String> {
        return listOfExecutingMethods
    }
}
