package com.tech4bytes.mbrosv3.AppData

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class testCall {

    fun t() {
        var t = UtilClass.get(false)
        LogMe.log("t size: " + t.size)
        t.forEach {
            LogMe.log("1. data: ${it.t1} , ${it.t2}")
        }
        val u = modelClass()
        u.t2 = 89.toString();
        u.t1 = 78.toString();
        UtilClass.saveToServerThenLocal(u)

        t = UtilClass.get()
        LogMe.log("t size: " + t.size)
        t.forEach {
            LogMe.log("data: ${it.t1} , ${it.t2}")
        }
    }
}