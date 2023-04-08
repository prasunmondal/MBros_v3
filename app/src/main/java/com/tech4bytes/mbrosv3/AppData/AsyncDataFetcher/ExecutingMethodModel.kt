package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import kotlin.reflect.KFunction

class ExecutingMethodModel: java.io.Serializable {
    var method: KFunction<Any>
    var description: String = "Get Data"
    var useCache = true

    constructor(method: KFunction<Any>, description: String = "Get Data", useCache: Boolean = true) {
        this.method = method
        this.description = description
        this.useCache = useCache
    }
}
