package org.akanework.checker.detection.utils

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object SystemProperties {

    private const val GETPROP_EXECUTABLE_PATH = "/system/bin/getprop"
    private const val TAG = "Checker"

    fun get(propName: String, defaultName: String): String {

        var process: Process? = null
        var bufferedReader: BufferedReader? = null

        return try {
            process = ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName)
                .redirectErrorStream(true).start()
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line = bufferedReader.readLine()
            if (line == null) {
                line = "" //prop not set
            }
            Log.i(TAG, "read System Property: $propName=$line")
            line
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read System Property $propName", e)
            defaultName
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (_: IOException) {
                }
            }
            process?.destroy()
        }
    }
}