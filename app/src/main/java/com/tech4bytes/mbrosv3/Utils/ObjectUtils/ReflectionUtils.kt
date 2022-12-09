package com.tech4bytes.mbrosv3.Utils.ObjectUtils

import kotlin.reflect.KMutableProperty1

class ReflectionUtils {
    companion object {
        fun <T> getAttributeNameAsString(kMutableProperty1: KMutableProperty1<T, String>): String {
            return kMutableProperty1.name
        }

        @Suppress("UNCHECKED_CAST")
        fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
            val property = instance::class.members
                // don't cast here to <Any, R>, it would succeed silently
                .first { it.name == propertyName } as KMutableProperty1<Any, *>
            // force a invalid cast exception if incorrect type here
            return property.get(instance) as R
        }

//        @Suppress("UNCHECKED_CAST")
//        fun <T> getAllAttributesOfClass(): ArrayList<KMutableProperty1<T, String>> {
//            var list: ArrayList<KMutableProperty1<T, String>> = arrayListOf()
//            (T.javaClass.simpleName).declaredMemberProperties.forEach {
//                list.add(it as KMutableProperty1<T, String>)
//                LogMe.log(it.name)
//            }
//            return list
//        }

//        @Suppress("UNCHECKED_CAST")
//        fun <T> getAttributeOfClass(name: String): KMutableProperty1<T, String>? {
//            (ExpenseData::class).declaredMemberProperties.forEach {
//                if(it.name == name)
//                    return it as KMutableProperty1<T, String>
//            }
//            return null
//        }

        fun <T> setAttribute(self: T, aProperty: KMutableProperty1<T, String>, internalIntValue: String) {
            aProperty.set(self, internalIntValue)
        }
    }
}