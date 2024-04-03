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
    var khataDue: String = "",
    var paidCash: String = "",
    var paidOnline: String = "",
    var paid: String = "",
    var totalBalance: String = "",
    var deliveryStatus: String = "",
    var adjustments: String = "",
    var notes: String = "",
    var otherBalances: String = "",
) : java.io.Serializable {

    override fun toString(): String {
        return "DeliverToCustomerDataModel(id='$id', date='$date', timestamp='$timestamp', name='$name', orderedPc='$orderedPc', orderedKg='$orderedKg', deliveredPc='$deliveredPc', deliveredKg='$deliveredKg', rate='$rate', todaysAmount='$todaysAmount', customerAccount='$customerAccount', prevDue='$prevDue', totalDue='$khataDue', paidCash='$paidCash', paidOnline='$paidOnline', paid='$paid', balanceDue='$totalBalance', deliveryStatus='$deliveryStatus', discount='$adjustments', notes='$notes')"
    }
}