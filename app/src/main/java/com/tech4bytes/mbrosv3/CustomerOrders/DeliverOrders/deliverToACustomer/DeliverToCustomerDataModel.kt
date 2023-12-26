package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

data class DeliverToCustomerDataModel(
    var id: String = "",
    var date: String = "",
    var timestamp: String = "",
    var name: String = "",
    var orderedPc: String = "",
    var orderedKg: String = "",
    var deliveredPc: String = "",
    var deliveredKg: String = "",
    var rate: String = "",
    var todaysAmount: String = "",
    var customerAccount: String = "",
    var prevDue: String = "",
    var totalDue: String = "",
    var paidCash: String = "",
    var paidOnline: String = "",
    var paid: String = "",
    var balanceDue: String = "",
    var deliveryStatus: String = "",
    var notes: String = "",
) : java.io.Serializable