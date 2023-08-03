/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.text.TextUtils
import android.util.Patterns

/**
 * Utility class that provides some useful methods for data validation
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object Validator {

    /**
     * Validate mail address
     *
     * @param mailAddress mail address to validate
     * @return true if mail address is valid
     */
    fun validateMail(mailAddress: String?): Boolean {
        return !TextUtils.isEmpty(mailAddress) && Patterns.EMAIL_ADDRESS.matcher(mailAddress!!).matches()
    }

    /**
     * Validate website url
     *
     * @param url the url to validate
     * @return true if mail address is valid
     */
    fun validateUrl(url: String?): Boolean {
        return !TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url!!).matches()
    }
}