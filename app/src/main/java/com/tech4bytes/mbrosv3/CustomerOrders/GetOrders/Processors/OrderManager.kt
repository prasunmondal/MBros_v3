package com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.Processors

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.GetOrders.GetCustomerOrders

class OrderManager {
    companion object {
        private var orderList: MutableMap<String, GetCustomerOrders> = mutableMapOf()

        fun saveToLocal(obj: MutableMap<String, GetCustomerOrders>) {
            SingleAttributedData.getRecords().order_data = serialize(orderList)
            SingleAttributedData.saveToLocal()
        }

        fun saveToServer(obj: MutableMap<String, GetCustomerOrders>) {
            saveToLocal(obj)
            SingleAttributedData.getRecords().order_data = serialize(orderList)
            SingleAttributedData.save()
        }

        fun getList(): MutableMap<String, GetCustomerOrders> {
            orderList = deserialize(SingleAttributedData.getRecords().order_data)
            return orderList
        }

        private fun deserialize(jsonString: String): MutableMap<String, GetCustomerOrders> {
            val typeRef: TypeReference<MutableMap<String?, GetCustomerOrders?>?> = object : TypeReference<MutableMap<String?, GetCustomerOrders?>?>() {}
            return ObjectMapper().readValue(jsonString, typeRef)
        }

        private fun serialize(obj: MutableMap<String, GetCustomerOrders>): String {
            return ObjectMapper().writeValueAsString(obj)
        }
    }
}