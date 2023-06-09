package com.example.albatong.employer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.data.*
import com.example.albatong.databinding.EmployerActivityStoreListBinding
import com.example.albatong.databinding.EmployerDialogStoreAddBinding
import com.example.albatong.er.ERActivityNotificationList
import com.example.albatong.er.ERActivitySetting
import com.example.albatong.er.ERActivitySpecificMain
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class EmployerActivityStoreList : AppCompatActivity() {
    lateinit var binding: EmployerActivityStoreListBinding
    lateinit var bindingDialog: EmployerDialogStoreAddBinding
    lateinit var adapter: EmployerAdapterStoreList
    lateinit var rdb: DatabaseReference
    lateinit var sdb: DatabaseReference
    lateinit var layoutManager: LinearLayoutManager
    var user_id:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EmployerActivityStoreListBinding.inflate(layoutInflater)
        bindingDialog = EmployerDialogStoreAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val i = intent
        user_id = i.getStringExtra("user_id")

        init()
        initRecyclereView()
    }

    override fun onResume() {
        super.onResume()
        adapter.startListening()
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    fun init() {
        binding.employerStoreAdd.setOnClickListener {
            storeAddDlg()
        }
        binding.employerSettingButton.setOnClickListener {
            val i = Intent(this@EmployerActivityStoreList, ERActivitySetting::class.java)
            startActivity(i)
        }
        binding.employerNotificationHistoryButton.setOnClickListener {
            val i = Intent(this@EmployerActivityStoreList, ERActivityNotificationList::class.java)
            startActivity(i)
        }
    }
    fun initRecyclereView() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rdb = Firebase.database.getReference("Users/employer/${user_id}/store")
        sdb = Firebase.database.getReference("Stores")
        val query = rdb.orderByChild("storeName")
        val option = FirebaseRecyclerOptions.Builder<StoreList>()
            .setQuery(query, StoreList::class.java)
            .build()
        adapter = EmployerAdapterStoreList(option)
        binding.apply {
            storeListRecyclerView.layoutManager = layoutManager
            storeListRecyclerView.adapter = adapter
            adapter.itemClickListener = object : EmployerAdapterStoreList.OnItemClickListener {
                override fun OnItemClick(store_id: String, store_name:String) {
                    binding.apply {
                        val i = Intent(this@EmployerActivityStoreList, ERActivitySpecificMain::class.java)
                        i.putExtra("store_id",store_id)
                        i.putExtra("store_name",store_name)
                        startActivity(i)
                    }
                }
            }
        }
    }

    fun storeAddDlg(){
        val builder = AlertDialog.Builder(this)

        val parentView = bindingDialog.root.parent as? ViewGroup
        parentView?.removeView(bindingDialog.root)
        builder.setView(bindingDialog.root)

        builder
            .setTitle("매장 추가")
            .setPositiveButton("OK"){
                    _,_ ->
                var id = generateRandomAlphabet()
                rdb.child(id).setValue(StoreList(bindingDialog.employerStoreAddNameEditText.text.toString(),id))
                sdb.child(id).setValue(Store(id,
                    StoreInfo(bindingDialog.employerStoreAddNameEditText.text.toString(),
                        user_id+"",
                        bindingDialog.employerStoreAddAddressEditText.text.toString(),
                        bindingDialog.employerStoreAddTelEditText.text.toString()),
                    StoreManager()
                ))
            }.setNegativeButton("Cancel"){
                    dlg , _ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.setOnDismissListener {
            // 다이얼로그가 닫힐 때, EditText를 초기화합니다.
            bindingDialog.employerStoreAddNameEditText.setText("")
            bindingDialog.employerStoreAddAddressEditText.setText("")
            bindingDialog.employerStoreAddTelEditText.setText("")
        }
        dlg.show()
    }
    fun generateRandomAlphabet(): String {
        val alphabet = ('A'..'Z')
        val number = ('0'..'9')
        val random = Random(System.currentTimeMillis())
        val randomAlphabet = (1..3).map { alphabet.random(random) }
        val randomNumber = (1..3).map { number.random(random) }
        return (randomAlphabet+randomNumber).joinToString("")
    }
}