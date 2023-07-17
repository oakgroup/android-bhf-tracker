/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.network

import android.text.TextUtils
import androidx.annotation.WorkerThread
import uk.ac.shef.tracker.BuildConfig
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.ThreadHandler.backgroundThread
import uk.ac.shef.tracker.core.utils.ThreadHandler.mainThread
import java.io.*
import java.net.HttpURLConnection

/**
 * Class used to build a connection using an instance of [TrackerWebService]
 *
 * @author omar.brugna
 */
class TrackerConnection(private val webService: TrackerWebService, private val listener: TrackerConnectionListener) {

    private var mInputStream: InputStream? = null
    private var mInputStreamReader: InputStreamReader? = null
    private var mBufferedReader: BufferedReader? = null

    var tag = Constants.INVALID

    private var timeoutExtended = false
    fun timeoutExtended(boolean: Boolean): TrackerConnection {
        timeoutExtended = boolean
        return this
    }

    fun connect() {
        backgroundThread {
            initConnection()
        }
    }

    @WorkerThread
    private fun initConnection() {
        mainThread {
            listener.onConnectionStarted(tag)
        }

        var response: String? = null
        try {
            response = startConnection()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Exception on connection ${webService.urlString} ${e.localizedMessage}")
        }

        if (response != null) {
            Logger.i("Connection response [${webService.urlString}]: $response")
            mainThread {
                listener.onConnectionSuccess(tag, response)
            }
        } else {
            Logger.w("Connection response [${webService.urlString}] is null")
            mainThread {
                listener.onConnectionError(tag)
            }
        }

        mainThread {
            // always notify completed request
            listener.onConnectionCompleted(tag)
        }
    }

    @Throws(IOException::class)
    private fun startConnection(): String? {

        if (BuildConfig.DEBUG) {
            val params = StringBuilder()
            if (!TextUtils.isEmpty(webService.params)) {
                val divider = "**********\n"
                params.append(divider)
                params.append(webService.params!!)
                params.append("\n")
                params.append(divider)
            }
            Logger.d(
                "Connection " +
                        "\nApi: " + webService.urlString +
                        "\nMethod: " + webService.method +
                        "\nTimeout: " + if (timeoutExtended) "Extended" else "Normal" +
                        "\nParams: " + params.toString()
            )
        }

        val connection = webService.url.openConnection() as HttpURLConnection
        connection.connectTimeout = if (timeoutExtended) TrackerNetwork.CONNECTION_TIMEOUT_EXTENDED else TrackerNetwork.CONNECTION_TIMEOUT
        connection.readTimeout = if (timeoutExtended) TrackerNetwork.SOCKET_TIMEOUT_EXTENDED else TrackerNetwork.SOCKET_TIMEOUT
        connection.useCaches = false
        connection.requestMethod = webService.method
        if (!TextUtils.isEmpty(webService.contentType)) connection.setRequestProperty(TrackerNetwork.CONTENT_TYPE, webService.contentType)
        if (!TextUtils.isEmpty(webService.connection)) connection.setRequestProperty(TrackerNetwork.CONNECTION, webService.connection)
        if (!TextUtils.isEmpty(webService.cacheControl)) connection.setRequestProperty(TrackerNetwork.CACHE_CONTROL, webService.cacheControl)
        if (webService.headers.isNotEmpty()) {
            for (header in webService.headers)
                connection.setRequestProperty(header.key, header.value)
        }

        if (!TextUtils.isEmpty(webService.params)) {
            val outputStream = BufferedOutputStream(connection.outputStream)
            try {
                if (!TextUtils.isEmpty(webService.params)) {
                    outputStream.write(webService.params!!.toByteArray(charset(webService.encoding)))
                } else {
                    Logger.w("Empty parameters on do output connection")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.e("Exception writing params")
            } finally {
                try {
                    outputStream.flush()
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.e("Exception closing params writer")
                }
            }
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Logger.e("Unexpected HTTP response: " + connection.responseCode + " " + connection.responseMessage)
            return null
        }

        try {
            val inputStream = connection.inputStream

            // read the response
            mInputStream = BufferedInputStream(inputStream)
            if (mInputStream != null) {
                mInputStreamReader = InputStreamReader(mInputStream!!, TrackerNetwork.ENCODING_UTF8)
                mBufferedReader = BufferedReader(mInputStreamReader!!)
                val stringBuilder = StringBuilder()
                var line: String?
                do {
                    line = mBufferedReader!!.readLine()
                    if (line == null) break
                    stringBuilder.append(line).append("\n")
                } while (true)

                return stringBuilder.toString().trim { it <= ' ' }

            } else Logger.e("Input stream is null")

        } catch (e: IOException) {
            Logger.w("Connection error: " + e.localizedMessage)
            e.printStackTrace()
        } finally {
            // clean up
            connection.disconnect()
            try {
                if (mInputStream != null)
                    mInputStream!!.close()
                if (mInputStreamReader != null)
                    mInputStreamReader!!.close()
                if (mBufferedReader != null)
                    mBufferedReader!!.close()
            } catch (ignored: IOException) {

            }
        }
        return null
    }
}