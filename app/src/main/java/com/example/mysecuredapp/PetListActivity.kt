package com.example.mysecuredapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysecuredapp.model.Pet
import kotlinx.android.synthetic.main.activity_pet_list.*
import java.io.File

class PetListActivity : AppCompatActivity() {

    private var petList: ArrayList<Pet> = ArrayList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter

    private val viewModel: PetViewModel by lazy {
        ViewModelProviders.of(this).get(PetViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_list)

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        adapter = RecyclerAdapter(petList)
        recyclerView.adapter = adapter

        setupPets()
    }

    private fun setupPets() {
        val file = File(filesDir.absolutePath + File.separator +
                MainActivity.FileConstants.DATA_SOURCE_FILE_NAME)
        val password = intent.getCharArrayExtra(PWD_KEY)
        petList.addAll( viewModel.getPets(file, password))
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val PWD_KEY = "PWD"
    }
}
