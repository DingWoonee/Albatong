package com.example.albatong.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.R
import com.example.albatong.data.SignData
import com.example.albatong.databinding.ActivitySignAcitivityBinding
import com.example.albatong.employer.EmployerActivityStoreList
import com.google.firebase.database.FirebaseDatabase

class SignAcitivity : AppCompatActivity() {
    lateinit var binding:ActivitySignAcitivityBinding
    lateinit var adapter: SignAdapter
    var data2: ArrayList<SignData> = ArrayList()
    var storeId = EmployerActivityStoreList.settingStoreId2
    var userId = LoginActivity.uId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
    }

    private fun init() {
        val emplo = FirebaseDatabase.getInstance().getReference("Users")
        var test = 0

        emplo.get().addOnSuccessListener {
            if(it.child("employee").child(userId).exists()){
                while(true){
                    if(it.child("employee").child(userId.toString()).child("Sign").child(test.toString()).exists()){
                            data2.add(SignData(it.child("employee").child(userId.toString()).child("Sign").child(test.toString()).child("title").getValue().toString(),
                            it.child("employee").child(userId.toString()).child("Sign").child(test.toString()).child("date").getValue().toString(),
                            it.child("employee").child(userId.toString()).child("Sign").child(test.toString()).child("type").getValue().toString()))
                            test++
                    }
                    else
                        break
                }
                var b:SignData = data2[0]
                var c:SignData

                if(data2.size>1){
                    data2[0] = data2[data2.size-1]
                    c = data2[1]
                    data2[1] = b
                    for(i in data2.size-2 downTo 1){
                        if(i==1)
                            data2[2] = c
                        else
                            data2[i+1] = data2[i]
                    }

                }
                adapter.notifyDataSetChanged()
            }

            if(it.child("employer").child(userId).exists()){
                while(true){
                    if(it.child("employer").child(userId.toString()).child("Sign").child(test.toString()).exists()){
                            data2.add(SignData(it.child("employer").child(userId).child("Sign").child(test.toString()).child("title").getValue().toString(),
                            it.child("employer").child(userId.toString()).child("Sign").child(test.toString()).child("date").getValue().toString(),
                            it.child("employer").child(userId.toString()).child("Sign").child(test.toString()).child("type").getValue().toString()))
                            test++
                    }
                    else
                        break
                }
                var b:SignData = data2[0]
                var c:SignData

                if(data2.size>1){
                    data2[0] = data2[data2.size-1]
                    c = data2[1]
                    data2[1] = b
                    for(i in data2.size-2 downTo 1){
                        if(i==1)
                            data2[2] = c
                        else
                            data2[i+1] = data2[i]
                    }

                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun initRecyclerView() {
        binding.recyclerView2.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        adapter = SignAdapter(data2)
        adapter.itemClickListener = object : SignAdapter.OnItemClickListener {
            override fun OnItemClick(data: SignData, position: Int) {
                val builder = AlertDialog.Builder(this@SignAcitivity)
                builder.setTitle("알림")

                var inflater = layoutInflater
                val dialogView = inflater.inflate(R.layout.sign_detail,null)
                val dialogTitle = dialogView.findViewById<TextView>(R.id.sign_title)
                val dialogDate = dialogView.findViewById<TextView>(R.id.sign_date)

                dialogTitle.text = data.title
                dialogDate.text = data.date

                builder.setView(dialogView)
                builder.setCancelable(false)

                builder.setPositiveButton("확인"){
                        p0,p1->{


                }
                }


                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
        binding.recyclerView2.adapter = adapter

    }
}