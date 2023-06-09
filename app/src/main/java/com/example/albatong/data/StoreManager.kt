package com.example.albatong.data


data class StoreManager(var announcements: ArrayList<String>,
                        var request: RequestManager,
                        var calendar: HashMap<String, HashMap<String, HashMap<String, Schedule>>>?=null){
    constructor(): this(ArrayList<String>(), RequestManager())
}
