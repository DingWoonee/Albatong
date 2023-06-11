package com.example.albatong.data

data class Schedule(var name: String,
                    var storeName: String,
                    var startTime:String,
                    var endTime: String,
                    var salary: Int=10000,
                    var store_id: String = "no-data") {
    constructor():this("name", "storename", "startTime", "endTime")
}
