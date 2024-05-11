package com.tech4bytes.mbrosv3.BusinessLogic

import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCModel
import com.tech4bytes.mbrosv3.Utils.ObjectUtils.ReflectionUtils
import java.util.Collections
import java.util.stream.IntStream
import kotlin.reflect.KMutableProperty1

class Sorter {
    companion object {
        fun sortByNameList(list: List<*>, nameAttribute: KMutableProperty1<*, *>): List<*> {
            val sortedList = CustomerKYC.get()
            Collections.sort(list,
                Comparator.comparing { item -> getCustomerIndex(sortedList, item, nameAttribute) })
            return list
        }

        private fun getCustomerIndex(sortedList: List<CustomerKYCModel>, item: Any, nameAttribute: KMutableProperty1<*, *>): Int {
            return IntStream.range(0, sortedList.size)
                .filter { i -> sortedList[i].nameEng == ReflectionUtils.readInstanceProperty<String>(item, nameAttribute.name) }
                .findFirst()
                .orElse(-1)
        }
    }
}