package com.tech4bytes.mbrosv3.Utils.T4B

import java.util.Arrays
import java.util.TreeSet
import java.util.stream.Collectors

class StringUtils {

    companion object {
        fun getListFromCSV(str: String): List<String> {
            val fruitList = Arrays.stream(str.split(",").toTypedArray())
                .map { str -> str.trim() }
                .collect(Collectors.toCollection { TreeSet() })
            return fruitList.toList()
        }
    }
}