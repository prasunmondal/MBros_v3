package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders

import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.util.stream.Collectors

class GetCustomerOrdersUtils {
    companion object {

        fun getCompleteList(): List<GetCustomerOrders> {
            val list: MutableList<GetCustomerOrders> = mutableListOf()
            val actualOrders = GetCustomerOrders.getServerList()
            CustomerKYC.getAllCustomers().forEach { masterList ->
                var isInOrderList = false
                actualOrders.forEach { orderList ->
                    if (masterList.nameEng == orderList.name) {
                        list.add(orderList)
                        isInOrderList = true
                    }
                }
                if (!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                    list.add(GetCustomerOrders(name = masterList.nameEng))
                }
            }
            return list
        }

        fun getListOfOrderedCustomers(): List<GetCustomerOrders> {
            val list: MutableList<GetCustomerOrders> = mutableListOf()
            val actualOrders = GetCustomerOrders.getServerList()
            CustomerKYC.getAllCustomers().forEach { masterList ->
                actualOrders.forEach { orderList ->
                    if (masterList.nameEng == orderList.name) {
                        list.add(orderList)
                    }
                }
            }
            return list
        }

        fun getListOfUnOrderedCustomers(): List<GetCustomerOrders> {
            val list: MutableList<GetCustomerOrders> = mutableListOf()
            val actualOrders = GetCustomerOrders.getServerList()
            CustomerKYC.getAllCustomers().forEach { masterList ->
                var isInOrderList = false
                actualOrders.forEach { orderList ->
                    if (masterList.nameEng == orderList.name) {
                        isInOrderList = true
                    }
                }
                if (!isInOrderList && masterList.isActiveCustomer.toBoolean()) {
                    list.add(GetCustomerOrders(name = masterList.nameEng))
                }
            }
            return list
        }

        fun getNumberOfCustomersOrdered(useCache: Boolean): Int {
            return GetCustomerOrders.get(useCache).size
        }

        fun getByName(inputName: String): GetCustomerOrders? {
            GetCustomerOrders.get().forEach {
                if (it.name == inputName) {
                    return it
                }
            }
            return null
        }

        fun updateObj(passedObj: GetCustomerOrders) {
            var toBeRemoved: GetCustomerOrders? = null
            GetCustomerOrders.obj.forEach {
                if (it.name == passedObj.name) {
                    toBeRemoved = it
                }
            }
            if (toBeRemoved != null) {
                GetCustomerOrders.obj.remove(toBeRemoved)
                GetCustomerOrders.obj.add(passedObj)
                LogMe.log("Updated: $passedObj")
            }

            val nameMappedOrders: MutableMap<String, GetCustomerOrders> = GetCustomerOrders.obj.stream()
                .collect(Collectors.toMap(GetCustomerOrders::name) { v -> v })
            LogMe.log(nameMappedOrders.toString())

            GetCustomerOrders.obj = mutableListOf()

            LogMe.log(CustomerKYC.getAllCustomers().toString())
            CustomerKYC.getAllCustomers().forEach {
                if (it.isActiveCustomer.toBoolean()) {
                    GetCustomerOrders.obj.add(nameMappedOrders[it.nameEng]!!)
                }
            }
            GetCustomerOrders.saveToLocal()
        }

        fun getRecordsForOnlyOrderedCustomers(): MutableList<GetCustomerOrders> {
            return GetCustomerOrders.obj.stream().filter { p -> p.id.isNotEmpty() }.collect(Collectors.toList())
        }

    }
}