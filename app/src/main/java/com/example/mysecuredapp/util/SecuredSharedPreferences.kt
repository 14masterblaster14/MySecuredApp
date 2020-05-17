package com.example.mysecuredapp.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.provider.Settings
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.example.mysecuredapp.PetApplication
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import kotlin.coroutines.coroutineContext


//const val SHARED_PREFERENCES = "MY_SECURED_PREFERENCES"


object SecuredSharedPreferences  {

    val PREFERENCE_NAME = "APP_SECURED_PREFERENCE"

    fun formatKeys(){


    }

    fun storeKeys( key:String,  value:String) {

        with(PetApplication().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()){
                putString(key,value)
                .apply()
        }
    }

}

/*: SharedPreferences{


    val PREFS_NAME = "SECURE_PREFS"

    protected val UTF8 = "utf-8"

    private var SEKRIT = ""

    protected var delegate: SharedPreferences? = null

    protected var context: Context? = null

    private var keyPreferences: SharedPreferences? = null

    fun EncryptSharedPreferences(context: Context?) {
        this.context = context
    }

    fun EncryptSharedPreferences(
        context: Context?,
        delegate: SharedPreferences?
    ) {
        this.delegate = delegate
        this.context = context
    }

    override fun edit(): Editor? {
        return Editor()
    }

    override fun getAll(): Map<String?, *>? {
        throw UnsupportedOperationException()
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val v = delegate!!.getString(key, null)
        return if (v != null) java.lang.Boolean.parseBoolean(decrypt(v)) else defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        val v = delegate!!.getString(key, null)
        return if (v != null) decrypt(v).toFloat() else defValue
    }

    override fun getInt(key: String?, defValue: Int): Int {
        val v = delegate!!.getString(key, null)
        return if (v != null) decrypt(v).toInt() else defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        val v = delegate!!.getString(key, null)
        return if (v != null) decrypt(v).toLong() else defValue
    }

    override fun getString(key: String?, defValue: String?): String? {
        val v = delegate!!.getString(key, null)
        DLog.d("shared pref encrypted string ======>", "" + v)
        return v?.let { decrypt(it) } ?: defValue
    }

    @Nullable
    override fun getStringSet(
        key: String?,
        defValues: Set<String?>?
    ): Set<String?>? {
        val stringSet = delegate!!.getStringSet(key, null)
        return stringSet?.let { decrypt(it) } ?: defValues
    }

    override fun contains(s: String?): Boolean {
        return delegate!!.contains(s)
    }

    override fun registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener?) {
        delegate!!.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener?) {
        delegate!!.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    protected fun encrypt(values: Set<String?>): Set<String>? {
        val encyptedSet: MutableSet<String> = HashSet()
        for (value in values) {
            encyptedSet.add(encrypt(value))
        }
        return encyptedSet
    }

    protected fun decrypt(values: Set<String?>): Set<String?>? {
        val decyptedSet: MutableSet<String?> = HashSet()
        for (value in values) {
            decyptedSet.add(decrypt(value))
        }
        return decyptedSet
    }

    protected fun encrypt(value: String?): String {
        return try {
            val bytes =
                value?.toByteArray(charset(UTF8)) ?: ByteArray(0)
            val keyFactory =
                SecretKeyFactory.getInstance("PBEWithMD5AndDES")
            loadRandomString()
            val key: SecretKey =
                keyFactory.generateSecret(PBEKeySpec(SEKRIT.toCharArray()))
            val pbeCipher = Cipher.getInstance("PBEWithMD5AndDES")
            pbeCipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                PBEParameterSpec(
                    Settings.Secure.getString(
                        context?.getContentResolver(),
                        Settings.Secure.ANDROID_ID
                    ).getBytes(UTF8), 20
                )
            )
            String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), UTF8)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadRandomString() {
        if (keyPreferences == null) {
            keyPreferences = context?.getSharedPreferences("KEY_PREF", Context.MODE_PRIVATE)
        }
        if ("" == SEKRIT) {
            SEKRIT = keyPreferences!!.getString(PREFS_NAME, "")!!
            if ("" == SEKRIT) {
                SEKRIT = SessionIdentifierGenerator().nextSessionId()
                keyPreferences!!.edit().putString(PREFS_NAME, SEKRIT).apply()
            }
        }
    }

    protected fun decrypt(value: String?): String {
        return try {
            val bytes =
                if (value != null) Base64.decode(value, Base64.DEFAULT) else ByteArray(0)
            val keyFactory =
                SecretKeyFactory.getInstance("PBEWithMD5AndDES")
            loadRandomString()
            val key: SecretKey =
                keyFactory.generateSecret(PBEKeySpec(SEKRIT.toCharArray()))
            val pbeCipher = Cipher.getInstance("PBEWithMD5AndDES")
            pbeCipher.init(
                Cipher.DECRYPT_MODE,
                key,
                PBEParameterSpec(
                    Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID
                    ).getBytes(UTF8), 20
                )
            )
            String(pbeCipher.doFinal(bytes), UTF8)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    class Editor : SharedPreferences.Editor {
        protected var delegate: SharedPreferences.Editor
        override fun putBoolean(key: String, value: Boolean): Editor {
            delegate.putString(key, encrypt(java.lang.Boolean.toString(value)))
            return this
        }

        override fun putFloat(key: String, value: Float): Editor {
            delegate.putString(key, encrypt(java.lang.Float.toString(value)))
            return this
        }

        override fun putInt(key: String, value: Int): Editor {
            delegate.putString(key, encrypt(Integer.toString(value)))
            return this
        }

        override fun putLong(key: String, value: Long): Editor {
            delegate.putString(key, encrypt(java.lang.Long.toString(value)))
            return this
        }

        override fun putString(key: String, value: String?): Editor {
            delegate.putString(key, encrypt(value))
            DLog.d("shared pref key", "" + key)
            DLog.d("shared pref value", "" + value)
            return this
        }

        fun clearString(key: String): Editor {
            delegate.putString(key, null)
            DLog.d("clearing pref key", "" + key)
            return this
        }

        override fun putStringSet(
            key: String,
            values: Set<String>?
        ): SharedPreferences.Editor {
            delegate.putStringSet(key, encrypt(values))
            return this
        }

        override fun apply() {
            delegate.apply()
        }

        override fun clear(): Editor {
            delegate.clear()
            return this
        }

        override fun commit(): Boolean {
            return delegate.commit()
        }

        override fun remove(s: String): Editor {
            delegate.remove(s)
            return this
        }

        init {
            delegate = this@EncryptSharedPreferences.delegate.edit()
        }
    }

    class SessionIdentifierGenerator {
        private val random = SecureRandom()
        fun nextSessionId(): String {
            return BigInteger(130, random).toString(32)
        }
    }
*/

    /* fun encrypt(
         dataToEncrypt: ByteArray,
         password: CharArray
     ): HashMap<String, ByteArray> {

         val map = HashMap<String, ByteArray>()

         val random = SecureRandom() //  cryptographically strong random number generator
         val salt = ByteArray(256)
         random.nextBytes(salt)

         val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
         // iterationCount :The higher the number, the longer it would take to operate
         // on a set of keys during a brute force attack.
         val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
         //PBKDF2withHmacSHA1<Supported API level 10+>
         val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
         val keySpec = SecretKeySpec(keyBytes, "AES")    // AES supported by API 1+

         val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
         val iv = ByteArray(16)
         ivRandom.nextBytes(iv)  //  Created 16 bytes of random data
         val ivSpec = IvParameterSpec(iv)    //  Packaged it into an IvParameterSpec object

         val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
         //  AES/CBC/PKCS7Padding supported by API 1+
         //  blocks are 128 bits long and AES adds padding before encryption.
         //  PKCS7Padding is a well-known standard for padding.

         cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
         val encrypted = cipher.doFinal(dataToEncrypt) // doFinal does the actual encryption.

         // Storing the required parameters in map for decryption
         map["salt"] = salt
         map["iv"] = iv
         map["encrypted"] = encrypted

         return map
     }

     fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

         var decrypted: ByteArray? = null

         //  Restoring the parameters from the map required for decryption
         val salt = map["salt"]
         val iv = map["iv"]
         val encrypted = map["encrypted"]

         //  regenerate key from password
         val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
         val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
         val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
         val keySpec = SecretKeySpec(keyBytes, "AES")

         //  Decrypting the data and returned it as a ByteArray
         val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
         val ivSpec = IvParameterSpec(iv)
         cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
         decrypted = cipher.doFinal(encrypted)

         return decrypted
     }


} */