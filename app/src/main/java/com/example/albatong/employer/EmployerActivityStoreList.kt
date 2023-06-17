package com.example.albatong.employer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.albatong.data.*
import com.example.albatong.databinding.EmployerActivityStoreListBinding
import com.example.albatong.databinding.EmployerDialogStoreAddBinding
import com.example.albatong.er.ERActivityNotificationList
import com.example.albatong.er.ERActivitySpecificMain
import com.example.albatong.er.ERsettingActivity
import com.example.albatong.login.SignAcitivity
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class EmployerActivityStoreList : AppCompatActivity() {
    lateinit var binding: EmployerActivityStoreListBinding
    lateinit var adapter: EmployerAdapterStoreList
    lateinit var rdb: DatabaseReference
    lateinit var sdb: DatabaseReference
    lateinit var layoutManager: LinearLayoutManager
    var user_id:String? = null
    private var backKeyPressedTime: Long = 0
    val storelist: ArrayList<String> = ArrayList()

    companion object{
        var settingUserId2:String? = null
        var settingStoreId2:String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EmployerActivityStoreListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var ab = FirebaseDatabase.getInstance().getReference("Stores").child("Storename")

        ab.get().addOnSuccessListener {
            var test=0
            while(true){
                if(it.child(test.toString()).exists()){
                    storelist.add(it.child(test.toString()).value.toString())
                    test++
                }
                else{
                    break
                }
            }
        }

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

    override fun onBackPressed() {
        // 현재 시간이 마지막으로 뒤로 가기 버튼을 눌렀던 시간보다 2초 이상 크면
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            return
        }
        // 마지막 '뒤로 가기'버튼 누르기 후, 2초가 지나지 않은 상태에서 '뒤로 가기'버튼을 누르면
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish() // 앱 종료
        }
    }

    fun init() {
        binding.employerStoreAdd.setOnClickListener {
            storeAddDlg()
        }
        binding.employerSettingButton.setOnClickListener {
            val i = Intent(this@EmployerActivityStoreList, ERsettingActivity::class.java)
            startActivity(i)
        }
        user_id = intent.getStringExtra("user_id")
        binding.employerNotificationHistoryButton.setOnClickListener {
            val i = Intent(this@EmployerActivityStoreList, SignAcitivity::class.java)
            i.putExtra("user_id", user_id)
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
                        settingStoreId2 = store_id
                        i.putExtra("store_id",store_id)
                        i.putExtra("store_name",store_name)
                        i.putExtra("user_id", user_id)
                        startActivity(i)
                    }
                }
            }
        }
    }

    fun storeAddDlg(){
        val bindingDialog = EmployerDialogStoreAddBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)

        val parentView = bindingDialog.root.parent as? ViewGroup
        parentView?.removeView(bindingDialog.root)

        val dlg = builder.setView(bindingDialog.root).show()

        dlg.window?.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlg.window?.setGravity(Gravity.CENTER)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDialog.registerBtn.setOnClickListener {
            var id = generateRandomAlphabet()
            rdb.child(id).setValue(StoreList(bindingDialog.employerStoreAddNameEditText.text.toString(),id))
            sdb.child(id).setValue(Store(id,
                StoreInfo(bindingDialog.employerStoreAddNameEditText.text.toString(),
                    user_id+"",
                    bindingDialog.employerStoreAddAddressEditText.text.toString(),
                    bindingDialog.employerStoreAddTelEditText.text.toString()),
                StoreManager()
            ))

            storelist.add(id.toString())
            FirebaseDatabase.getInstance().getReference("Stores").child("Storename").setValue(storelist)

            dlg.dismiss()
        }


        bindingDialog.cancelBtn.setOnClickListener {
            dlg.dismiss()

        }

        dlg.setOnDismissListener {
            // 다이얼로그가 닫힐 때, EditText를 초기화합니다.
            bindingDialog.employerStoreAddNameEditText.setText("")
            bindingDialog.employerStoreAddAddressEditText.setText("")
            bindingDialog.employerStoreAddTelEditText.setText("")
        }
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