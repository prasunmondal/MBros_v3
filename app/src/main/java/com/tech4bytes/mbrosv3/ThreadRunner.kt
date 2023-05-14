package com.tech4bytes.mbrosv3

class ThreadRunner : Thread() {
    fun run(function: () -> (Unit)) {
        function.invoke()
    }
}