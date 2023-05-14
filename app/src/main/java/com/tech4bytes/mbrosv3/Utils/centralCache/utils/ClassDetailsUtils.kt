package com.tech4bytes.extrack.centralCache.utils

import kotlin.reflect.KClass

class ClassDetailsUtils {

    companion object {

        /**
        Returns the calling method.
        Example: com.tech4bytes.extrack.models.ExpenseData
         */
        fun getCaller(): String {
            val parentPackage = "com.tech4bytes.extrack.centralCache"
            var startSearching = false

            /*
                The stacktrace starts with some thread classes.
                So, first reach to the current package where this code is present
                and the as soon as we exit from this current package, return the first value after that
             */
            Thread.currentThread().stackTrace.forEach { trace ->
                if (!startSearching && trace.className.startsWith(parentPackage))
                    startSearching = true

                if (startSearching && !trace.className.startsWith(parentPackage))
                    return Class.forName(trace.className).name
            }
            return ""
        }

        fun <T : Any> getClassName(clazz: KClass<T>): String {
            return clazz.qualifiedName + ""
        }
    }
}