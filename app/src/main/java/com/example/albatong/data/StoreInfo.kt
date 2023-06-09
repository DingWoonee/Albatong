package com.example.albatong.data

data class StoreInfo(var storeName: String,
                     var employerId: String,
                     var address: String,
                     var tel:String) {
    constructor(): this("no-data", "no-data","no-data", "no-data")
}