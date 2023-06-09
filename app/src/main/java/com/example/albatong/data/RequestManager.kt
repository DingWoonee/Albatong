package com.example.albatong.data
// 수정
data class RequestManager(var date: String,
                          var startTime: String,
                          var endTime: String,
                          var sender: String,
                          var receiver: String,
                          var state: Int)
{
    constructor(): this("no-data", "0:00", "0:00", "no-data", "no-data", 99)
}
