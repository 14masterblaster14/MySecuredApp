package com.example.mysecuredapp

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *      Encrypting the data with Advanced Encryption Standard (AES) which uses
 *      substitution–permutation network to encrypt your data with a key.
 *      It uses the Symmetric Encryption with PBKDF2 for key wherein it takes password and by
 *      hashing it with random data many times over, it creates a key.The random data is called salt.
 *      This creates a strong and unique key, even if someone else uses the same password.
 *
 *                          |--> Iteration Count ------>|
 *                          |                           |
 *      User Password ------|------------------------> PBKDF2 ---> AES Key
 *                          |                           |
 *                          |--> Salt ----------------->|
 *
 *      AES works in different modes, standard mode is cipher block chaining (CBC).
 *     CBC encrypts your data one chunk at a time.CBC is secure because each block of data in the
 *     pipeline is XOR’d with the previous block that it encrypted.So to start with first block we
 *     will use an initialization vector (IV) which generates block of random data that gets XOR’d
 *     with that first block. So This means that identical sets of data encrypted with the same key
 *     will not produce identical outputs.
 *
 *     Note ::  On Android 4.3 and under, there was a vulnerability with SecureRandom.
 *     It had to do with improper initialization of the underlying pseudo random number generator
 *     (PRNG). A fix is available if you support Android 4.3 and under.
 *
 *
 *      Note:   API < 18 Confidential data should be stored in local encrypted file.
 *              API >= 23 Confidential data should be stored in Keystore.
 *
 *      Note:   Asymmetric keys supports API >= 18
 *              Symmetric keys supports API >= 23
 *
 *
 *
 *      KeyGenerator — provides the public API for generating symmetric cryptographic keys.
 *
 *      KeyPairGenerator — an engine class which is capable of generating a private key and
        its related public key utilizing the algorithm it was initialized with.

        SecretKey — a secret (symmetric) key. The purpose of this interface is to group
        (and provide type safety for) all secret key interfaces (e.g., SecretKeySpec).

        PrivateKey — a private (asymmetric) key. The purpose of this interface is to group
        (and provide type safety for) all private key interfaces(e.g., RSAPrivateKey).

        PublicKey — a public key. This interface contains no methods or constants.It merely serves
        to group (and provide type safety for) all public key interfaces(e.g., RSAPublicKey).

        KeyPair —   this class is a simple holder for a key pair (a public key and a private key).
        It does not enforce any security, and, when initialized, should be treated like a PrivateKey.

        SecureRandom — generates cryptographically secure pseudo-random numbers. We will not use it
        directly in this series, but it is widely used inside of KeyGenerator,
        KeyPairGenerator components and Keys implementations.

        KeyStore — database with a well secured mechanism of data protection, that is used to save,
        get and remove keys. Requires entrance password and passwords for each of the keys.
        In other words it is protected file that you need to create, read and update (with provided API).

        Certificate — certificate used to validate and save asymmetric keys.

        Cipher — provides access to implementations of cryptographic ciphers for encryption,
        decryption, wrapping, unwrapping and signing.

        Provider — defines a set of extensible implementations, independent API’s. Providers are
        the groups of different Algorithms or their customizations.

 For API <23 & >=18
https://code.tutsplus.com/tutorials/keys-credentials-and-storage-on-android--cms-30827
https://www.apriorit.com/dev-blog/432-using-androidkeystore
https://medium.com/@ericfu/securely-storing-secrets-in-an-android-application-501f030ae5a3

 */

internal class Encryption {

    fun encrypt(
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

    //@RequiresApi(Build.VERSION_CODES.N)   //  setUserAuthenticationValidWhileOnBody
    @TargetApi(Build.VERSION_CODES.M)     //  KeyGenParameterSpec
    fun keystoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {

        val map = HashMap<String, ByteArray>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {    // Symmetric keys supports only API >= 23

            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "MyKeyAlias",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                //.setUserAuthenticationRequired(true) // requires lock screen, invalidated if lock screen is disabled
                //.setUserAuthenticationValidityDurationSeconds(120) // key will be available only 120 seconds from password authentication. -1 requires finger print - every time
                //.setUserAuthenticationValidWhileOnBody(boolean remainsValid) //It makes the key unavailable once the device has detected it is no longer on the person.
                .setRandomizedEncryptionRequired(true) // different ciphertext for same plaintext on each call by using new IV each time
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()

            //Get the key from KeyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKeyEntry =
                keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            //Encrypt data
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivBytes = cipher.iv
            val encryptedBytes = cipher.doFinal(dataToEncrypt)

            // return a HashMap containing the encrypted data and IV needed to decrypt the data
            map["iv"] = ivBytes
            map["encrypted"] = encryptedBytes


        }   //else {    // Asymmetric keys supports API >= 18 & <23}


        return map
    }

    fun keystoreDecrypt(map: HashMap<String, ByteArray>): ByteArray? {

        var decrypted: ByteArray? = null

        //Get the key from KeyStore
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry =
            keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        //Extract the necessary info from map
        val encryptedBytes = map["encrypted"]
        val ivBytes = map["iv"]

        //Decrypt the data to a ByteArray by initializing the Cipher object using the DECRYPT_MODE constant
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        decrypted = cipher.doFinal(encryptedBytes)

        return decrypted
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @TargetApi(23)
    fun keystoreTest() {

        // Created a test string and encrypted it
        val map = keystoreEncrypt("My very sensitive string!".toByteArray(Charsets.UTF_8))
        // decrypt method on the encrypted output to test that everything worked
        val decryptedBytes = keystoreDecrypt(map)
        decryptedBytes?.let {
            val decryptedString = String(it, Charsets.UTF_8)
            Log.e("MyApp", "The decrypted string is: $decryptedString")
        }

        /* fun getAllAliasesInTheKeystore() : ArrayList<String> {

             private ArrayList<String> getAllAliasesInTheKeystore() throws KeyStoreException {
                 return Collections.list(keyStore.aliases());
             }
          }*/
    }
}