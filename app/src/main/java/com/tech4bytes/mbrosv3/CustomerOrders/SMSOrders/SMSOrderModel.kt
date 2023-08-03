package com.tech4bytes.mbrosv3.CustomerOrders.SMSOrders

data class SMSOrderModel(var name: String, var orderedKg: Int, var calculatedPc: Double, var finalizedPc: Int) {
}