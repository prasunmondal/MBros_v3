package com.tech4bytes.mbrosv3.AppData

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class testCall {

    fun t() {
        var t = modelClass().get<modelClass>()
        t.forEach { it ->
            LogMe.log("data t1: " + it.t1)
            LogMe.log("data t2: " + it.t2)
        }

        t = modelClass().get()
        t.forEach { it ->
            LogMe.log("data t1: " + it.t1)
            it.t1 = "90"
            LogMe.log("data t2: " + it.t2)
            it.t2 = "80"
        }
        LogMe.log(t[0].t1)
        LogMe.log(t[0].t2)

        modelClass().saveToServer(t[0])
    }
}