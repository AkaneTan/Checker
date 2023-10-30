package org.akanework.checker.utils

import java.io.BufferedReader
import java.io.InputStreamReader

object CheckerUtils {

    fun checkGnssHal(): MutableList<String> {
        try {
            val command = "lshal | grep gnss"
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            val gnssList = mutableListOf<String>()
            while (reader.readLine().also { line = it } != null) {
                line?.let { gnssList.add(it) }
            }

            process.waitFor()
            return gnssList

        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }
    }

}