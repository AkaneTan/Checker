package org.akanework.checker.detection.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

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
        return if (output.joinToString().contains(" Y ")) 1 else 0
    }

    data class APKSignature (
        val issuer: String = "unknown",
        val subject: String = "unknown",
        val certificate: String = "Invalid"
    )

    @Suppress("DEPRECATION")
    fun getSignatureX509(packageManager: PackageManager, packageName: String): List<APKSignature> {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val signatures: Array<Signature> = packageInfo.signatures

            if (signatures.isNotEmpty()) {
                val certificateList = mutableListOf<APKSignature>()
                for (signature in signatures) {
                    val certificateFactory = CertificateFactory.getInstance("X.509")
                    val byteArrayInputStream = ByteArrayInputStream(signature.toByteArray())
                    val x509Certificate =
                        certificateFactory.generateCertificate(byteArrayInputStream) as X509Certificate

                    certificateList.add(
                        APKSignature(
                            issuer = x509Certificate.issuerDN.name,
                            subject = x509Certificate.subjectDN.name,
                            certificate = getPEMCertificate(x509Certificate)
                        )
                    )
                }

                return certificateList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return listOf()
    }

    private fun getPEMCertificate(certificate: X509Certificate): String {
        val base64Cert = Base64.encodeToString(certificate.encoded, Base64.DEFAULT)
        return "-----BEGIN CERTIFICATE-----\n$base64Cert-----END CERTIFICATE-----"
    }

}