/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.text.TextUtils
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Utility class that exposes some encryption methods
 */
object Encryption {

    private const val ALGORITHM_SHA256 = "SHA-256"

    fun hash(string: String?): String {
        if (!TextUtils.isEmpty(string)) {
            try {
                val digest = MessageDigest.getInstance(ALGORITHM_SHA256)
                digest.update(string!!.toByteArray())
                val messageDigest = digest.digest()
                return convertToHex(messageDigest)
            } catch (e: NoSuchAlgorithmException) {
                Logger.e("NoSuchAlgorithmException exception during encryption")
            } catch (e: UnsupportedEncodingException) {
                Logger.e("UnsupportedEncodingException exception during encryption")
            }
        } else {
            Logger.e("Trying to hash an empty string")
        }
        return Constants.EMPTY
    }

    private fun convertToHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (byte in bytes) {
            val hex = Integer.toHexString(0xFF and byte.toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }
}