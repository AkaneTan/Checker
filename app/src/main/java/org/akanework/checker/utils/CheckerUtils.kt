package org.akanework.checker.utils

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object CheckerUtils {

    private val commandCache = mutableMapOf<String, List<String>>()

    private fun executeShellCommand(command: String): List<String> {
        // Check if the result is already cached
        if (commandCache.containsKey(command)) {
            return commandCache[command]!!
        }

        val outputLines = mutableListOf<String>()

        try {
            val processBuilder = ProcessBuilder("sh", "-c", command)
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                line?.let { outputLines.add(it) }
            }

            while (errorReader.readLine().also { line = it } != null) {
                // Handle errors if needed
            }

            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Cache the result for future use
        commandCache[command] = outputLines

        return outputLines
    }

    fun checkHals(): List<String> {
        val command = "lshal"
        return executeShellCommand(command)
    }

    fun isSELinuxEnforcing(): Int {
        val command = "lshal"
        val output = executeShellCommand(command)
        Log.d("TAG", output.toString())
        return if (output.joinToString().contains(" Y ")) 1 else 0
    }
}