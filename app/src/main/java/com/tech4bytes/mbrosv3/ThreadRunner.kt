package com.tech4bytes.mbrosv3

import kotlin.reflect.KFunction

class ThreadRunner: Thread() {
    fun run(function: () -> (Unit)) {
        function.invoke()
    }
}