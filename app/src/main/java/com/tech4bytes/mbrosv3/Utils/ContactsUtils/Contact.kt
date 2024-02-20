package com.tech4bytes.mbrosv3.Utils.ContactsUtils

class Contact {
    var name: String? = null
    var phoneNumber: String? = null

    constructor(name: String?, phoneNumber: String?) {
        this.name = name
        this.phoneNumber = phoneNumber
    }
    override fun toString(): String {
        return "Contact(name=$name, phoneNumber=$phoneNumber)"
    }
}