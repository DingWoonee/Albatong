package com.example.albatong.data

data class UserData(
    var user_id:String,
    var password:String,
    var name:String,
    var tel:String,
    var email:String,
    var store: HashMap<String, StoreList>?=null
) {
    constructor():this("noinfo","noinfo","noinfo","noinfo","noinfo")
}
