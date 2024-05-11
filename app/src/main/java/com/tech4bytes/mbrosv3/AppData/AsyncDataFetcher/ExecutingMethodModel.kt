package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

class ExecutingMethodModel : java.io.Serializable {
    var method: () -> List<Any>
    var description: String = "Get Data"
    var useCache = true

    constructor(func: () -> List<Any>, description: String = "Get Data", useCache: Boolean = true) {
        this.method = func
        this.description = description
        this.useCache = useCache
    }
}
