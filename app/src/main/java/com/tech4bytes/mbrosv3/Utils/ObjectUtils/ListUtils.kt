package com.tech4bytes.mbrosv3.Utils.ObjectUtils

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.util.stream.Collectors
import kotlin.reflect.KMutableProperty1


class ListUtils {
    companion object {
        fun sortListByFrequency(list: Array<String>): Array<String> {
            val elementCountMap: MutableMap<String, Int> = LinkedHashMap()

            for (i in 0 until list.size) {
                if (elementCountMap.containsKey(list.get(i))) {
                    //If element is present in elementCountMap, increment its value by 1
                    elementCountMap[list.get(i)] = elementCountMap[list.get(i)]!! + 1
                } else {
                    //If element is not present, insert this element with 1 as its value
                    elementCountMap[list.get(i)] = 1
                }
            }

            val result =
                elementCountMap.toList().sortedBy { (_, value) -> value }.reversed().toMap()
            LogMe.log("Frequency Map")
            LogMe.log("" + result)
            return ArrayList(result.keys).toTypedArray()
        }

        fun <T> sortListByAttribute(records: List<T>, attribute: KMutableProperty1<T, String>): List<T> {
            return records.stream().sorted(Comparator.comparing(attribute)).collect(Collectors.toList())!!
        }

        fun <T> getFilteredResultsFilterByAttribute(data: List<T>, attribute: KMutableProperty1<T, String>, value: String): List<T> {
            return getFilteredResultsFilterByAttribute(data, ReflectionUtils.getAttributeNameAsString(attribute), value)
        }

        fun <T> removeObjectsByAttributeValue(data: List<T>, attributeName: KMutableProperty1<T, String>, value: String): List<T> {
            val attributeNameString = ReflectionUtils.getAttributeNameAsString(attributeName)
            val result = data.stream().filter { d ->
                ReflectionUtils.readInstanceProperty<String>(
                    d as Any,
                    attributeNameString
                ).toString().lowercase() != value.lowercase()
            }.collect(Collectors.toList()).toList()
            return result
        }

        fun <T> getFilteredResultsFilterByAttribute(data: List<T>, attributeName: String, value: String): List<T> {
            LogMe.log("Filtering Results:: $attributeName: $value")
            if(value.isEmpty())
                return data
            val result = data.stream().filter { d ->
                ReflectionUtils.readInstanceProperty<String>(
                    d as Any,
                    attributeName
                ).toString().equals(value, true)
            }.collect(Collectors.toList())
            return result
        }

        fun <T> getAllPossibleValuesList(data: List<T>, attribute: KMutableProperty1<T, String>): List<String> {
            var attributeName = ReflectionUtils.getAttributeNameAsString(attribute)
            return data.stream().map {
                ReflectionUtils.readInstanceProperty<String>(
                    it as Any,
                    attributeName
                )
            }.collect(Collectors.toList())
        }

        fun <T> removeDuplicates(list: List<T>): List<T> {
            return ArrayList(HashSet(list))
        }

        fun <T> getSumOfAttribute(data: List<T>, attribute: KMutableProperty1<T, String>): Double {
            var attributeName = ReflectionUtils.getAttributeNameAsString(attribute)
            val sum: Double = data.stream()
                .mapToDouble { x ->
                    if(ReflectionUtils.readInstanceProperty<String>(x as Any, attributeName).isEmpty()) {
                        0.0
                    } else {
                        ReflectionUtils.readInstanceProperty<String>(x as Any, attributeName).toDouble()
                    }
                }.sum()

            return sum
        }
    }
}