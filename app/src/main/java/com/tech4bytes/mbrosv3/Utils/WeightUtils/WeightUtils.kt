package com.tech4bytes.mbrosv3.Utils.WeightUtils

import java.text.DecimalFormat

class WeightUtils {
    companion object {

        fun roundOff3places(number: Double): Double {
            val df = DecimalFormat("#.###")
            return df.format(number).toDouble()
        }

        fun roundOff3places(number: String): Double {
            val df = DecimalFormat("#.###")
            return df.format(number.toDouble()).toDouble()
        }
    }
}