package com.tech4bytes.mbrosv3.Utils.Contexts

import android.content.Context
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class AppContexts {

    companion object {
        var contexts: MutableMap<String, Context> = mutableMapOf()

        fun set(obj: Any, context: Context) {
            val className = obj.javaClass.canonicalName.replace(".Companion", "")
            LogMe.log("Setting context: $className")
            contexts[className] = context
        }

        @JvmStatic
        fun get(): Context {
            val getCallerClassname = getCaller()
            LogMe.log("Asking context: $getCallerClassname")
            var contextResult: Context? = null
            contexts.forEach { classname, context ->
                if (getCallerClassname.startsWith(classname))
                    contextResult = context
            }
            if (contextResult == null) {
                getCaller(true)
                contextResult = contexts[contexts.keys.first()]!!.applicationContext
            }
            return contextResult!!
        }

        fun getCaller(printStackTrace: Boolean = false): String {
            val stackTraceElements = Thread.currentThread().stackTrace
            if (printStackTrace) {
                stackTraceElements.forEach { trace ->
                    println(trace.toString())
                }
            }
            var traceClassnameMatch = ""
            stackTraceElements.forEach { trace ->
                contexts.forEach { classname, context ->
                    var clazzName = classname.replace(".Companion", "")
                    if (trace.className.startsWith(clazzName)) {
                        if (traceClassnameMatch.isEmpty())
                            traceClassnameMatch = clazzName
                    }
                }
            }
            return traceClassnameMatch
        }
    }
}