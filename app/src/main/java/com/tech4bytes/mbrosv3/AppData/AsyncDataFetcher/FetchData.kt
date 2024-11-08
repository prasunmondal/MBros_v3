package com.tech4bytes.mbrosv3.AppData.AsyncDataFetcher

import android.view.View

data class FetchData(
    var view: View,
    var label: String,
    var executingMethod: () -> Unit,
    var useCache: Boolean,
    var isCompleted: Boolean,
) {}
