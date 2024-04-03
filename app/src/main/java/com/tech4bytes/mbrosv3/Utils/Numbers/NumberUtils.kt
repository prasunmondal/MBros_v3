package com.tech4bytes.mbrosv3.Utils.Numbers

import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import java.math.RoundingMode
import java.text.DecimalFormat

class NumberUtils {
    companion object {

        fun getIntOrZero(input: String?): Int {
            return try {
                input!!.toInt()
            } catch (e: Exception) {
                0
            }
        }

        fun getIntOrBlank(input: String): String {
            val t = getIntOrZero(input)
            if (t == 0)
                return ""
            return t.toString()
        }

        fun getDoubleOrZero(input: String): Double {
            return try {
                input.toDouble()
            } catch (e: Exception) {
                0.0
            }
        }

        fun getDoubleOrBlank(input: String): String {
            val t = getDoubleOrZero(input)
            if (t == 0.0)
                return ""
            return t.toString()
        }

        fun getDoubleOrZero(input: String, roundOffPattern: String): Double {
            val number = getDoubleOrZero(input)
            return try {
                val df = DecimalFormat(roundOffPattern)
                df.roundingMode = RoundingMode.FLOOR
                df.format(number).toDouble()
            } catch (e: Exception) {
                number
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

        fun roundOff2places(number: Double): Double {
            val df = DecimalFormat("#.##")
            return df.format(number).toDouble()
        }

        fun roundOff3places(number: Double): Double {
            val df = DecimalFormat("#.###")
            return df.format(number).toDouble()
        }
    }
}