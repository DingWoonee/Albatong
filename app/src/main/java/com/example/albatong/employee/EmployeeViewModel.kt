package com.example.albatong.employee

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.albatong.data.UserData

class EmployeeViewModel:ViewModel() {
    val selectedUser = MutableLiveData<UserData>()

    fun setLiveData(user: UserData) {
        selectedUser.value = user
    }
}