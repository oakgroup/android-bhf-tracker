/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

@file:Suppress("DEPRECATION")

package uk.ac.shef.tracker.core.database.engine.encryption

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import uk.ac.shef.tracker.core.database.engine.TrackerDatabase
import uk.ac.shef.tracker.core.utils.Logger
import java.math.BigInteger
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.security.auth.x500.X500Principal

/**
 * This class is used to generate an encrypted key pair and save it to the Android Keystore
 * Thi key pair is used to encrypt the [TrackerDatabase]
 */
object TrackerDatabaseKeystore {

    private const val TAG = "DatabaseKeystore"
    private const val AndroidKeyStore = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val RSA_MODE = "RSA"
    private const val KEY_ALIAS = "KEY_ALIAS"
    private val KEY_IV = byteArrayOf(7, 2, 3, 8, 4, 0, 3, 1, 4, 2, 5, 7)

    /**
     * Get the public encrypted key
     *
     * @param context an instance of the [Context]
     * @return the public encrypted key
     */
    fun getSecretKey(context: Context): String? {
        val key = generateOrGetKey(context)
        return encryptKey(key)
    }

    /**
     * Generate the key pair and save it to the Android Keystore
     *
     * @param context an instance of the [Context]
     * @return the public key
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun generateOrGetKey(context: Context): Key? {
        try {
            val keyStore = KeyStore.getInstance(AndroidKeyStore)
            keyStore.load(null)
            return if (!keyStore.containsAlias(KEY_ALIAS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build()
                    keyGenerator.init(keyGenParameterSpec)
                    keyGenerator.generateKey()
                } else {
                    val start = Calendar.getInstance()
                    val end = Calendar.getInstance()
                    end.add(Calendar.YEAR, 30)
                    val keyPairGeneratorSpec = KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(X500Principal("CN=$KEY_ALIAS"))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
                    val keyPairGenerator = KeyPairGenerator.getInstance(RSA_MODE, AndroidKeyStore)
                    keyPairGenerator.initialize(keyPairGeneratorSpec)
                    keyPairGenerator.generateKeyPair().public
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyStore.getKey(KEY_ALIAS, null)
                } else {
                    val certificate = keyStore.getCertificate(KEY_ALIAS)
                    certificate.publicKey
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(TAG, "Error on key generation")
        }
        return null
    }

    /**
     * Encrypt the public key
     *
     * @param key the [Key] to encrypt
     * @return the public key encrypted
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun encryptKey(key: Key?): String? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cipher = Cipher.getInstance(AES_MODE)
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, KEY_IV))
                val encodedBytes = cipher.doFinal()
                Base64.encodeToString(encodedBytes, Base64.DEFAULT)
            } else {
                val cipher = Cipher.getInstance(RSA_MODE)
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val encodedBytes = cipher.doFinal(KEY_IV)
                Base64.encodeToString(encodedBytes, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(TAG, "Error on key encryption")
        }
        return null
    }
}