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
    var discount: String = "",
    var notes: String = "",
) : java.io.Serializable {

    override fun toString(): String {
        return "DeliverToCustomerDataModel(id='$id', date='$date', timestamp='$timestamp', name='$name', orderedPc='$orderedPc', orderedKg='$orderedKg', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', todaysAmount='$todaysAmount', customerAccount='$customerAccount', prevDue='$prevDue', totalDue='$totalDue', paidCash='$paidCash', paidOnline='$paidOnline', paid='$paid', balanceDue='$balanceDue', deliveryStatus='$deliveryStatus', discount='$discount', notes='$notes')"
    }
}