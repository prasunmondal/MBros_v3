package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import android.view.View
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class FetchData(
    var view: View,
    var label: String,
    var executingMethod: KClass<Any>,
    var useCache: Boolean,
    var isCompleted: Boolean,
) {}
