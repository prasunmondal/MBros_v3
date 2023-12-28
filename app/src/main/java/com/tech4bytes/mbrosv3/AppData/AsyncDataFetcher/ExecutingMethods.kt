package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import java.lang.reflect.Method
import kotlin.reflect.KClass

class ExecutingMethods: java.io.Serializable {
    private var listOfExecutingMethods: MutableMap<KClass<Any>, ExecutingMethodModel> = mutableMapOf()

    fun add(clazz: KClass<Any>, useCache: Boolean = true) {
        listOfExecutingMethods[clazz] =
            ExecutingMethodModel(
                clazz,
                DataFetchingInfo.getDescription(clazz),
                useCache
            )
    }

    fun get(): MutableMap<KClass<Any>, ExecutingMethodModel> {
        return listOfExecutingMethods
    }
}
