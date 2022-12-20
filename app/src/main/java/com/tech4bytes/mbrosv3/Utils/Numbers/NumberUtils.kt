package com.tech4bytes.mbrosv3.Utils.Numbers

import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders

class NumberUtils {
    companion object {

        fun getIntOrZero(input: String): Int {
            return try {
                input.toInt()
            } catch (e: Exception) {
                0
            }
        }

        fun getDoubleOrZero(input: String): Double {
            return try {
                input.toDouble()
            } catch (e: Exception) {
                0.0
            }
        }
    }
}