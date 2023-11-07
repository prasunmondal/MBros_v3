package com.tech4bytes.mbrosv3.BusinessData

class SingleAttributedDataUtils {

    companion object {
        fun getBufferRateInt(): Int {
            if (SingleAttributedData.getRecords().bufferRate.isEmpty())
                return 0
            return SingleAttributedData.getRecords().bufferRate.toInt()
        }

        fun getFinalRateInt(): Int {
            if (SingleAttributedData.getRecords().finalFarmRate.isEmpty())
                return 0
            return SingleAttributedData.getRecords().finalFarmRate.toInt()
        }
    }
}
