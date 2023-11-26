package org.akanework.checker.detection

import android.app.Activity
import android.media.MediaDrm
import android.os.Build
import org.akanework.checker.detection.utils.CheckerUtils
import org.akanework.checker.detection.utils.SystemProperties
import java.util.UUID

/**
 * [CheckerMethods] is a series of detection / query
 * methods for the Checker app.
 *
 * @author Akane Beneckendorff
 */
object CheckerMethods {

    const val INVALID = "invalid"

    val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

    /**
     * [WidevineInfo] is a data class for storing widevine info
     *
     * @param drmLevel
     * @param drmVersion
     * @param drmVendorLevel
     * @param drmDesc
     * @param drmAlgo
     */
    data class WidevineInfo (
        val drmLevel: String = INVALID,
        val drmVersion: String = INVALID,
        val drmVendorLevel: String = INVALID,
        val drmDesc: String = INVALID,
        val drmAlgo: String = INVALID
    )

    /**
     * [getDrmInfo] is a method for querying widevine CDM.
     *
     * @return WidevineInfo
     */
    fun getDrmInfo() : WidevineInfo {
        val mediaDrmInstance = MediaDrm(WIDEVINE_UUID)
        return WidevineInfo(
            drmLevel = mediaDrmInstance.getPropertyString("securityLevel"),
            drmVersion = mediaDrmInstance.getPropertyString(MediaDrm.PROPERTY_VERSION),
            drmVendorLevel = mediaDrmInstance.getPropertyString(MediaDrm.PROPERTY_VENDOR),
            drmDesc = mediaDrmInstance.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION),
            drmAlgo = mediaDrmInstance.getPropertyString(MediaDrm.PROPERTY_ALGORITHMS)
        )
    }

    /**
     * [getGnssHidlHalList] is a method for querying gnss hidl list.
     *
     * @return String
     */
    fun getGnssHidlHalList() : String {
        val connectivityGNSSVersionRawList = CheckerUtils.checkHals()
        return connectivityGNSSVersionRawList
                .filter { it.contains("android.hardware.gnss") }
                .map {
                    it.substringBefore(":").substringAfterLast("? ").substringAfterLast("Y ").trim()
                }
                .distinct()
                .joinToString(separator = "\n")
    }

    /**
     * [getMediaHdrList] is a method for querying media HDR list.
     *
     * @return String
     */
    fun getMediaHdrList(activity: Activity): String {
        val mediaHdrIntList =
            if (Build.VERSION.SDK_INT >= 34) {
                activity.display!!.mode!!.supportedHdrTypes
            } else if (Build.VERSION.SDK_INT >= 30) {
                activity.display!!.hdrCapabilities!!.supportedHdrTypes
            } else {
                intArrayOf(0)
            }
        val mediaHdrStringList = mediaHdrIntList!!.map {
            when (it) {
                1 -> "Dolby vision"
                2 -> "HDR 10"
                3 -> "HLG"
                4 -> "HDR 10+"
                -1 -> "Invalid"
                else -> "Unknown"
            }
        }
        return mediaHdrStringList.joinToString(separator = ", ")
    }

    /**
     * [getSelinuxStatus] is a method for querying selinux stat.
     *
     * @return 0, 1, 2
     * 0 - Enforcing
     * 1 - Permissive
     * 2 - Invalid
     */
    fun getSelinuxStatus() : Int {
        return CheckerUtils.isSELinuxEnforcing()
    }

    /**
     * [getSystemSignKey] returns system's signature.
     * Can be one of: test-keys, release-keys, dev-keys, invalid
     *
     * @return String
     */
    fun getSystemSignKey() : String {
        val buildTags = SystemProperties.get("ro.system.build.fingerprint", "undefined")

        val keyType: Int = if (buildTags.contains("test-keys")) {
            0
        } else if (buildTags.contains("release-keys")) {
            1
        } else if (buildTags.contains("dev-keys")) {
            2
        } else {
            3
        }

        return when (keyType) {
            0 -> "test-keys"
            1 -> "release-keys"
            2 -> "dev-keys"
            else -> INVALID
        }
    }

    /**
     * [getVerifiedBootState] returns boot state.
     * Could be one of: green, yellow, orange, red.
     *
     * @return String
     */
    fun getVerifiedBootState() = SystemProperties.get("ro.boot.verifiedbootstate", "unknown")

    /**
     * [getTrebleEnabledState] returns treble support state.
     * Could be true or false.
     *
     * @return Boolean
     */
    fun getTrebleEnabledState() = SystemProperties.get("ro.treble.enabled", "false").toBoolean()


}