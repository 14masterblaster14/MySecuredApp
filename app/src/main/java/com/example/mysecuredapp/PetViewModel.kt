package com.example.mysecuredapp

import androidx.lifecycle.ViewModel
import com.example.mysecuredapp.model.Pet
import com.example.mysecuredapp.model.Pets
import org.simpleframework.xml.core.Persister
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

class PetViewModel : ViewModel() {

    private var pets: ArrayList<Pet>? = null

    fun getPets(file: File, password: CharArray): ArrayList<Pet> {
        if (pets == null) {
            loadPets(file, password)
        }

        return pets ?: arrayListOf()
    }

    private fun loadPets(file: File, password: CharArray) {


        var decrypted: ByteArray? = null
        ObjectInputStream(FileInputStream(file)).use { it ->
            val data = it.readObject()

            when (data) {
                is Map<*, *> -> {

                    if (data.containsKey("iv") && data.containsKey("salt") && data.containsKey("encrypted")) {
                        val iv = data["iv"]
                        val salt = data["salt"]
                        val encrypted = data["encrypted"]
                        if (iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {
                            decrypted = Encryption().decrypt(
                                hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted),
                                password
                            )
                        }
                    }
                }
            }
        }

        if (decrypted != null) {

            val serializer = Persister()
            //val inputStream = file.inputStream() //TODO: Replace me
            val inputStream = ByteArrayInputStream(decrypted)
            val pets = try {
                serializer.read(Pets::class.java, inputStream)
            } catch (e: Exception) {
                null
            }
            pets?.list?.let {
                this.pets = ArrayList(it)
            }
        }
    }

}