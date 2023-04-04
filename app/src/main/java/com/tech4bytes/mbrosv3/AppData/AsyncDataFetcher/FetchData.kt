package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import kotlin.reflect.KFunction

data class FetchData(var label: String, var executingMethod: KFunction<Any>) {}
