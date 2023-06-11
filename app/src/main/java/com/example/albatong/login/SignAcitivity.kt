package com.example.albatong.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.R
import com.example.albatong.data.SignData
import com.example.albatong.databinding.ActivitySignAcitivityBinding
import com.example.albatong.ee.EEMyData
import com.example.albatong.ee.EEMyDataAdapter
import com.google.firebase.database.FirebaseDatabase

class SignAcitivity : AppCompatActivity() {
    lateinit var binding:ActivitySignAcitivityBinding
    lateinit var adapter: SignAdapter
    var data2: ArrayList<SignData> = ArrayList()

  /*  companion object{
        lateinit var adapter: SignAdapter
        var data2: ArrayList<SignData> = ArrayList()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data2.add(SignData("중요공지가 등록되었습니다","1",1))
        initRecyclerView()
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