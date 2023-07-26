package com.tech4bytes.mbrosv3.Sms

class SMSModel {
    var number: String
    var datetime: String
    var body: String

    constructor(number: String, datetime: String, body: String) {
        this.number = number
        this.datetime = datetime
        this.body = body
    }
}