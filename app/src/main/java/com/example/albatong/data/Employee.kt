package com.example.albatong.data

data class Employee(var employeeId: String,
                    var name: String,
                    var salary: Int=10000) {
    constructor(): this("no-data", "no-data")

    // 20230609 test
    // 20230609 test5
}