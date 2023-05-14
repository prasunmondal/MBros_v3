package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import android.view.View
import kotlin.reflect.KFunction

data class FetchData(
    var view: View,
    var label: String,
    var executingMethod: KFunction<Any>,
    var useCache: Boolean,
    var isCompleted: Boolean
) {}
