package com.example.albatong.data

data class RequestManager(var schedule: Schedule,
                          var sender: String,
                          var senderName: String,
                          var receiver: String,
                          var state: Int)
{
    constructor(): this(Schedule(), "no-data", "no-data","no-data", 99)
}
