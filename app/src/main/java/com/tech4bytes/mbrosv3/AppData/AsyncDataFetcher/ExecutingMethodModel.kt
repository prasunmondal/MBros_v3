package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import kotlin.reflect.KClass

class ExecutingMethodModel: java.io.Serializable {
    var method: KClass<Any>
    var description: String = "Get Data"
    var useCache = true

    constructor(method: KClass<Any>, description: String = "Get Data", useCache: Boolean = true) {
        this.method = method
        this.description = description
        this.useCache = useCache
    }
}
