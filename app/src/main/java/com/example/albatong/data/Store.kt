package com.example.albatong.data

data class Store(var storeId: String,
                 var storeInfo: StoreInfo,
                 var storeManager: StoreManager,
                 var employee: HashMap<String, Employee>?=null)
{
    constructor(): this("no-data", StoreInfo(), StoreManager())
}