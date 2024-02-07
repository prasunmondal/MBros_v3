package com.tech4bytes.mbrosv3.CustomerOrders.Occasions

import java.io.Serializable

data class EventsModel(val id: String, val eng_date: String, val occassion_name: String): Serializable {

    override fun toString(): String {
        return "EventsModel(id='$id', eng_date='$eng_date', occassion_name='$occassion_name')"
    }
}
