package com.example.albatong.data

data class Employee(var employeeId: String,
                    var name: String,
                    var salary: Int=10000) {
    constructor(): this("no-data", "no-data")

    // 바꿈1
}