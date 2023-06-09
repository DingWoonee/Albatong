package com.example.albatong.data

import com.example.albatong.R

data class StoreList(var storeName: String,
                     var store_id:String,
                     var storeColor:Int = R.color.white){
    constructor():this("noinfo","noinfo")
}
