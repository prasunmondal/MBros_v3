package com.tech4bytes.mbrosv3.Utils.Numbers

import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverCustomerOrders
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.math.RoundingMode
import java.text.DecimalFormat

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

        fun roundDownDecimal3Places(number: Double): Double {
            LogMe.log("Rounding to 3 decimal places: " + number)
            return try {
                val df = DecimalFormat("#.###")
                df.roundingMode = RoundingMode.FLOOR
                df.format(number).toDouble()
            } catch (e: Exception) {
                number
            }
        }
    }
}