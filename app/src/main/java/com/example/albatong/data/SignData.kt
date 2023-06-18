package com.example.albatong.data

data class SignData(var title:String?,
                    var date:String?,
                    var type:String?,
                    var schedule: Schedule?=null,
                    var selectedDate: String?=null,
                    var dayOfWeek: String?=null) {
    constructor(): this("title", "date","type", Schedule(), "null", "null")
}
