package com.example.mysecuredapp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.text.DateFormat
import java.util.*


/**
 *  Refer : https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started
 *
 *  A>  Manifest Changes :
 *  <1> Install the app in internal memory only
 *  <2> As users can access the contents of the app’s private data folder using adb backup,
 *      Don't allow to take the backup of application.
 *
 *  B>  Encrypting the data with Advanced Encryption Standard (AES) which uses
 *      substitution–permutation network to encrypt your data with a key.
 *      It uses the Symmetric Encryption with PBKDF2 for key wherein it takes password and by
 *      hashing it with random data many times over, it creates a key.The random data is called salt.
 *      This creates a strong and unique key, even if someone else uses the same password.
 *
 *                          |-- Iteration Count---------|
 *                          |                           |
 *      User Password ------|------------------------> PBKDF2 ---> AES Key
 *                          |                           |
 *                          |---- Salt------------------|
 *
 *  C> Conceal (by Facebook) is a great choice for a third party encryption library.
 *  D> Account Manager with OAuth token
 *  E> Keychain API for PrivateKey and X509Certificate objects
 *  F> ProGuard for code obfuscation
 */


class MainActivity : AppCompatActivity() {

    private var isSignedUp = false
    private var workingFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        workingFile = File(filesDir.absolutePath + File.separator +
                FileConstants.DATA_SOURCE_FILE_NAME)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            Encryption().keystoreTest()
        }

        updateLoggedInState()
    }

    fun loginPressed(view: android.view.View) {

        var success = false
        val password = login_password.text.toString()

        //Check if already signed up
        if (isSignedUp) {

            val lastLogin = lastLoggedIn()
            if (lastLogin != null) {
                success = true
                toast("Last login: $lastLogin")
            } else {
                toast("Please check your password and try again.")
            }

        } else {
            when {
                password.isEmpty() -> toast("Please enter a password!")
                password == login_confirm_password.text.toString() -> workingFile?.let {
                    createDataSource("pets.xml", it)
                    success = true
                }
                else -> toast("Passwords do not match!")
            }
        }

        if (success) {

            saveLastLoggedInTime()

            //Start next activity
            val context = view.context
            val petListIntent = Intent(context, PetListActivity::class.java)
            petListIntent.putExtra(PWD_KEY, password.toCharArray())
            context.startActivity(petListIntent)
        }
    }

    private fun updateLoggedInState() {
        val fileExists = workingFile?.exists() ?: false
        if (fileExists) {
            isSignedUp = true
            button.text = getString(R.string.login)
            login_confirm_password.visibility = View.INVISIBLE
        } else {
            button.text = getString(R.string.signup)
        }
    }

    private fun lastLoggedIn(): String? {

        //Get password
        val password = CharArray(login_password.length())
        login_password.text.getChars(0, login_password.length(), password, 0)

        //Retrieve shared prefs data
        val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val base64Encrypted = preferences.getString("l", "")
        val base64Salt = preferences.getString("lsalt", "")
        val base64Iv = preferences.getString("liv", "")

        //Base64 decode on strings to convert them back to raw bytes.
        val encrypted = Base64.decode(base64Encrypted, Base64.NO_WRAP)
        val iv = Base64.decode(base64Iv, Base64.NO_WRAP)
        val salt = Base64.decode(base64Salt, Base64.NO_WRAP)

        //Decrypt
        val decrypted = Encryption().decrypt(
            hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted), password)

        var lastLoggedIn: String? = null
        decrypted?.let {
            lastLoggedIn = String(it, Charsets.UTF_8)
        }
        return lastLoggedIn


       /* //Retrieve shared prefs data
        val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return preferences.getString("l", "")*/
    }

    private fun saveLastLoggedInTime() {

        //Get password
        val password = CharArray(login_password.length())
        login_password.text.getChars(0, login_password.length(), password, 0)

        //Base64 the data
        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        // Converted the String into a ByteArray with the UTF-8 encoding and encrypted it.
        val map =
            Encryption().encrypt(currentDateTimeString.toByteArray(Charsets.UTF_8), password)
        // Converted the raw data into a String representation.
        val valueBase64String = Base64.encodeToString(map["encrypted"], Base64.NO_WRAP)
        val saltBase64String = Base64.encodeToString(map["salt"], Base64.NO_WRAP)
        val ivBase64String = Base64.encodeToString(map["iv"], Base64.NO_WRAP)

        // Saved the strings to the SharedPreferences
        val editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
        editor.putString("l", valueBase64String)
        editor.putString("lsalt", saltBase64String)
        editor.putString("liv", ivBase64String)
        editor.apply()

        /*val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())

        //Save to shared prefs
        val editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
        editor.putString("l", currentDateTimeString)
        editor.apply()*/
    }

    private fun createDataSource(filename: String, outFile: File) {

        val inputStream = applicationContext.assets.open(filename)  // Opening data file
        val bytes = inputStream.readBytes() //converting to data stream
        inputStream.close()

        val password = CharArray(login_password.length())
        login_password.text.getChars(0, login_password.length(), password, 0)
        //serialized the HashMap using the ObjectOutputStream class and then saved it to storage.
        val map = Encryption().encrypt(bytes, password)
        ObjectOutputStream(FileOutputStream(outFile)).use {
                it -> it.writeObject(map)
        }

        //This is just for demo data
       /* val fileDescriptor = applicationContext.assets.openFd(filename)
        fileDescriptor.createInputStream().use { inputStream ->
            FileOutputStream(outFile).use { outputStream ->
                inputStream.channel.transferTo(fileDescriptor.startOffset,
                    fileDescriptor.length,
                    outputStream.channel)
            }
        }*/
    }

    companion object {
        private const val PWD_KEY = "PWD"
    }

    object FileConstants {
        const val DATA_SOURCE_FILE_NAME = "pets.xml"
    }
}
