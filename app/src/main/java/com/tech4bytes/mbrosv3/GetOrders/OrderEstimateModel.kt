package com.tech4bytes.mbrosv3.GetOrders

data class OrderEstimateModel(var id: String, var name: String, var estimatePc: String, var rate: String, var due: String, var estimateKg: String) {

    override fun toString(): String {
        return "OrderEstimateModel(id='$id', name='$name', estimatePc='$estimatePc', rate='$rate', due='$due', estimateKg='$estimateKg')"
    }
}