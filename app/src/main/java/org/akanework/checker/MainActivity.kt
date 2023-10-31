package org.akanework.checker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT
import android.content.res.Configuration
import android.graphics.ColorSpace
import android.media.MediaDrm
import android.media.MediaDrm.PROPERTY_ALGORITHMS
import android.media.MediaDrm.PROPERTY_DESCRIPTION
import android.media.MediaDrm.PROPERTY_VENDOR
import android.media.MediaDrm.PROPERTY_VERSION
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.akanework.checker.utils.CheckerUtils
import org.akanework.checker.utils.SystemProperties
import java.util.UUID


class MainActivity : Activity() {

    companion object {
        val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
    }

    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var headerCard: LinearLayout
    private lateinit var headerCardFrame: MaterialCardView

    private val abnormalitiesList = mutableListOf<String>()
    private val abnormalitiesAdapter = EntryAdapter(abnormalitiesList)

    private var securityVerifiedBootState = "unknown"
    private var securityLevel = 0

    private fun changeTitleStatus(status: Int) {
        var titleString = getString(R.string.normal_title)
        var summaryString = getString(R.string.normal_summary)
        var targetColor: Int
        var targetTextColor: Int

        CoroutineScope(Dispatchers.Default).launch {
            when (status) {
                0 -> {
                    targetColor = MaterialColors.harmonizeWithPrimary(
                        this@MainActivity,
                        getColor(R.color.green)
                    )
                    targetTextColor = MaterialColors.harmonizeWithPrimary(
                        this@MainActivity,
                        getColor(R.color.colorOnGreen)
                    )
                }

                1 -> {
                    titleString = getString(R.string.notice_title)
                    summaryString = getString(R.string.notice_summary)
                    targetColor = MaterialColors.harmonizeWithPrimary(
                        this@MainActivity,
                        getColor(R.color.yellow)
                    )
                    targetTextColor = MaterialColors.harmonizeWithPrimary(
                        this@MainActivity,
                        getColor(R.color.colorOnYellow)
                    )
                }

                2 -> {
                    titleString = getString(R.string.critical_title)
                    summaryString = getString(R.string.critical_summary)
                    targetColor = MaterialColors.getColor(
                        titleTextView,
                        com.google.android.material.R.attr.colorErrorContainer
                    )
                    targetTextColor = MaterialColors.getColor(
                        titleTextView,
                        com.google.android.material.R.attr.colorOnErrorContainer
                    )
                }

                else -> throw IllegalAccessException()
            }
            withContext(Dispatchers.Main) {
                titleTextView.text = titleString
                summaryTextView.text = summaryString
                titleTextView.setTextColor(targetTextColor)
                summaryTextView.setTextColor(targetTextColor)
                headerCard.setBackgroundColor(targetColor)
                headerCardFrame.visibility = View.VISIBLE
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n", "HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
        }
        setContentView(R.layout.activity_main)

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.more -> {
                    InfoDialogFragment().show(fragmentManager, "DIALOG")
                    true
                }

                else ->
                    throw IllegalAccessException()
            }
        }

        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        headerCard = findViewById(R.id.header)
        headerCardFrame = findViewById(R.id.frame)

        val architectureSocTextView = findViewById<TextView>(R.id.architecture_soc_version)
        val architectureSupportedABITextView = findViewById<TextView>(R.id.architecture_supported)

        val basicAndroidVersionTextView = findViewById<TextView>(R.id.basic_android_version)
        val basicAndroidSdkLevelTextView = findViewById<TextView>(R.id.basic_sdk_level)
        val basicAndroidIdTextView = findViewById<TextView>(R.id.basic_android_id)
        val basicAndroidBrandTextView = findViewById<TextView>(R.id.basic_android_brand)
        val basicAndroidModelTextView = findViewById<TextView>(R.id.basic_android_model)
        val basicAndroidModelSKUTextView = findViewById<TextView>(R.id.basic_model_sku)
        val basicAndroidBuildTypeTextView = findViewById<TextView>(R.id.basic_build_type)
        val basicAndroidFingerprintTextView = findViewById<TextView>(R.id.basic_fingerprint)

        val widevineLevelTextView = findViewById<TextView>(R.id.widevine_hdcp_level)
        val widevineVersionLevelTextView = findViewById<TextView>(R.id.widevine_name_level)
        val widevineVendorLevelTextView = findViewById<TextView>(R.id.widevine_vendor_level)
        val widevineDescTextView = findViewById<TextView>(R.id.widevine_desc)
        val widevineAlgoTextView = findViewById<TextView>(R.id.widevine_algo)

        val connectivityRadioTextView = findViewById<TextView>(R.id.connectivity_radio)
        val connectivityGNSSHalStatus = findViewById<TextView>(R.id.connectivity_gps)

        val mediaHdrTypeTextView = findViewById<TextView>(R.id.media_hdr_types)
        val mediaWideColorGamutTextView = findViewById<TextView>(R.id.media_wide_color_gamut)

        val securitySELinuxStateTextView = findViewById<TextView>(R.id.security_selinux_state)
        val securityVerifiedBootStateTextView = findViewById<TextView>(R.id.security_verified_boot_state)
        val securityAndroidSignatureTextView = findViewById<TextView>(R.id.security_system_signature)

        val universalityTrebleTextView = findViewById<TextView>(R.id.universality_treble_support)
        val universalityGSITextView = findViewById<TextView>(R.id.universality_gsi_support)
        val universalityDSUTextView = findViewById<TextView>(R.id.universality_dsu_support)

        val abnormalitiesFrame = findViewById<MaterialCardView>(R.id.abnormal_frame)
        val abnormalitiesRecyclerView = findViewById<RecyclerView>(R.id.abnormal_recyclerview)
        abnormalitiesRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        abnormalitiesRecyclerView.adapter = abnormalitiesAdapter

        // Set up architecture info
        architectureSocTextView.text = if (Build.VERSION.SDK_INT >= 31)
            "${getString(R.string.architecture_soc_model)} - ${Build.SOC_MODEL}"
        else
            getString(R.string.architecture_failed_to_retrieve_soc)
        architectureSupportedABITextView.text =
            "${getString(R.string.architecture_supported_abi)} - ${Build.SUPPORTED_ABIS.joinToString(separator = ", ")}"


        // Set up basic info
        val basicAndroidVersion = Build.VERSION.RELEASE
        val basicSDKLevel = Build.VERSION.SDK_INT
        val basicAndroidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val basicBrand = Build.BRAND
        val basicModel = Build.MODEL
        val basicModelSKU =
            if (Build.VERSION.SDK_INT >= 31) Build.SKU else ""
        val basicBuildType = Build.TYPE
        val basicFingerprint = Build.FINGERPRINT
        basicAndroidVersionTextView.text =
            "${getString(R.string.basic_android_version)} - $basicAndroidVersion"
        basicAndroidSdkLevelTextView.text =
            "${getString(R.string.basic_android_sdk)} - $basicSDKLevel"
        basicAndroidIdTextView.text = "${getString(R.string.basic_android_id)} - $basicAndroidId"
        basicAndroidBrandTextView.text = "${getString(R.string.basic_brand)} - $basicBrand"
        basicAndroidModelTextView.text = "${getString(R.string.basic_model)} - $basicModel"
        if (Build.VERSION.SDK_INT >= 31) {
            basicAndroidModelSKUTextView.text =
                "${getString(R.string.basic_model_sku)} - $basicModelSKU"
        } else {
            basicAndroidModelSKUTextView.text =
                getString(R.string.basic_model_sku)
        }
        basicAndroidBuildTypeTextView.text =
            "${getString(R.string.basic_build_type)} - $basicBuildType"
        basicAndroidFingerprintTextView.text =
            "${getString(R.string.basic_build_fingerprint)} - $basicFingerprint"

        // Set up widevine info
        val widevineVersionQueryJob = CoroutineScope(Dispatchers.Default).async {
            val widevineKeyDrm = MediaDrm(WIDEVINE_UUID)
            val drmLevel = widevineKeyDrm.getPropertyString("securityLevel")
            val drmVersion = widevineKeyDrm.getPropertyString(PROPERTY_VERSION)
            val drmVendorLevel = widevineKeyDrm.getPropertyString(PROPERTY_VENDOR)
            val drmDesc = widevineKeyDrm.getPropertyString(PROPERTY_DESCRIPTION)
            val drmAlgo = widevineKeyDrm.getPropertyString(PROPERTY_ALGORITHMS)

            val isDrmPassing =
                if (drmLevel == "L1") 0 else if (drmLevel == "L2") 1 else if (drmLevel == "L3") 2 else 3

            if (isDrmPassing != 0) {
                abnormalitiesList.add(getString(R.string.abnormalities_widevine))
                if (securityLevel == 0) securityLevel = 1
            }

            withContext(Dispatchers.Main) {
                widevineLevelTextView.text = "${getString(R.string.drm_security_level)} - $drmLevel"
                widevineVersionLevelTextView.text = "${getString(R.string.drm_version)} - $drmVersion"
                widevineVendorLevelTextView.text = "${getString(R.string.drm_vendor)} - $drmVendorLevel"
                widevineDescTextView.text = "${getString(R.string.drm_desc)} - $drmDesc"
                widevineAlgoTextView.text = "${getString(R.string.drm_algo)} - $drmAlgo"
            }
        }

        // Set up radio info
        val connectivityRadioVersion = Build.getRadioVersion().substringAfterLast(',')
        val connectivityGNSSQueryJob = CoroutineScope(Dispatchers.Default).async {
            val connectivityGNSSVersionRawList = CheckerUtils.checkHals()
            val connectivityGNSSVersionList = connectivityGNSSVersionRawList
                .filter { it.contains("android.hardware.gnss") }
                .map {
                    it.substringBefore(":").substringAfterLast("? ").substringAfterLast("Y ").trim()
                }
                .distinct()
                .joinToString(separator = "\n")
            if (connectivityGNSSVersionList.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val anim = AlphaAnimation(1.0f, 0.0f)
                    anim.duration = 200
                    anim.repeatCount = 1
                    anim.repeatMode = Animation.REVERSE

                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {
                            connectivityGNSSHalStatus.text =
                                "${getString(R.string.connectivity_gnss)}\n$connectivityGNSSVersionList"
                        }
                    })

                    connectivityGNSSHalStatus.startAnimation(anim)
                }
            } else {
                withContext(Dispatchers.Main) {
                    val anim = AlphaAnimation(1.0f, 0.0f)
                    anim.duration = 200
                    anim.repeatCount = 1
                    anim.repeatMode = Animation.REVERSE

                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {
                            connectivityGNSSHalStatus.text =
                                getString(R.string.connectivity_gnss_not_found)
                        }
                    })

                    connectivityGNSSHalStatus.startAnimation(anim)
                }
            }
        }
        connectivityRadioTextView.text =
            "${getString(R.string.connectivity_radio)} - $connectivityRadioVersion"

        // Set up media info
        val mediaHdrIntList =
            if (Build.VERSION.SDK_INT >= 34) {
                this.display!!.mode!!.supportedHdrTypes
            } else if (Build.VERSION.SDK_INT >= 30) {
                this.display!!.hdrCapabilities!!.supportedHdrTypes
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

        val mediaHdrString = mediaHdrStringList.joinToString(separator = ", ")
        val mediaIsGrantedGamut =
            if (Build.VERSION.SDK_INT >= 26 && Configuration().isScreenWideColorGamut) getString(R.string.media_window_available) else getString(R.string.media_window_unavailable)

        mediaHdrTypeTextView.text =
            "${getString(R.string.media_supported_hdr_types)} - $mediaHdrString"
        mediaWideColorGamutTextView.text =
            "${getString(R.string.media_wide_color_gamut)} - $mediaIsGrantedGamut"

        // Set up security info
        val securitySELinuxQueryJob = CoroutineScope(Dispatchers.Default).async {
            val securityIsSELinuxEnforcing = CheckerUtils.isSELinuxEnforcing()
            withContext(Dispatchers.Main) {
                securitySELinuxStateTextView.text = getString(R.string.security_selinux_state) + " - " +
                    when (securityIsSELinuxEnforcing) {
                        0 -> "Enforcing"
                        1 -> {
                            abnormalitiesList.add(getString(R.string.abnormalities_selinux_not_enforcing))
                            securityLevel = 2
                            "Permissive"
                        }
                        2 -> {
                            "Invalid"
                        }
                        else -> throw IllegalArgumentException()
                    }
            }
        }
        val securityVerifiedBootStateQueryJob = CoroutineScope(Dispatchers.Default).async {
            securityVerifiedBootState = SystemProperties.get("ro.boot.verifiedbootstate", "unknown")
            if (securityVerifiedBootState != "green") {
                abnormalitiesList.add(getString(R.string.abnormalities_verified_boot_stat))
                if (securityLevel == 0) securityLevel = 1
            }
            withContext(Dispatchers.Main) {
                securityVerifiedBootStateTextView.text =
                    "${getString(R.string.security_verified_boot_state)} - $securityVerifiedBootState"
            }
        }
        val securityAndroidSigningKeyQueryJob = CoroutineScope(Dispatchers.Default).async {
            val buildTags = SystemProperties.get("ro.system.build.fingerprint", "undefined")
            var keyType: Int

            var signingkey = "undefined"

            if (buildTags.contains("test-keys")) {
                abnormalitiesList.add(getString(R.string.abnormalities_signed_using_a_publickey))
                if (securityLevel == 0) securityLevel = 1
                keyType = 0
            } else if (buildTags.contains("release-keys") || buildTags.contains("dev-keys")) {
                keyType = 1
                if (buildTags.contains("dev-keys")) keyType = 2
            } else {
                abnormalitiesList.add(getString(R.string.abnormalities_undefined_signing_key))
                securityLevel = 2
                keyType = 3
            }
            when (keyType) {
                0 -> signingkey = "test-keys"
                1 -> signingkey = "release-keys"
                2 -> signingkey = "dev-keys"
                3 -> signingkey = "undefined"
            }
            withContext(Dispatchers.Main) {
                securityAndroidSignatureTextView.text =
                    "${getString(R.string.security_signing_key)} - $signingkey"
            }
        }

        // Set up universality info
        val universalityTrebleQueryJob = CoroutineScope(Dispatchers.Default).async {
            val isTrebleSupported = SystemProperties.get("ro.treble.enabled", "false")
            withContext(Dispatchers.Main) {
                universalityTrebleTextView.text =
                    "${getString(R.string.universality_treble_support)} - $isTrebleSupported"
                universalityGSITextView.text =
                    if (isTrebleSupported == "true")
                        "${getString(R.string.universality_gsi_compatibility)} - ${getString(R.string.available)} (Treble)"
                    else
                        "${getString(R.string.universality_gsi_compatibility)} - ${getString(R.string.unavailable)}"
            }
        }
        universalityDSUTextView.text =
            if (Build.VERSION.SDK_INT >= 31)
                "${getString(R.string.universality_dsu_status)} - ${getString(R.string.available)}"
            else
                "${getString(R.string.universality_dsu_status)} - ${getString(R.string.unavailable)}"

        // Get abnormalities
        if (connectivityRadioVersion == "unknown") {
            abnormalitiesList.add(getString(R.string.abnormalities_baseband_broken))
            securityLevel = 2
        }

        CoroutineScope(Dispatchers.Default).launch {
            awaitAll(
                connectivityGNSSQueryJob,
                securitySELinuxQueryJob,
                securityVerifiedBootStateQueryJob,
                securityAndroidSigningKeyQueryJob,
                universalityTrebleQueryJob,
                widevineVersionQueryJob
            )
            withContext(Dispatchers.Main) {
                changeTitleStatus(securityLevel)
                if (abnormalitiesList.size != 0) {
                    abnormalitiesAdapter.notifyItemRangeInserted(0, abnormalitiesList.size)
                    abnormalitiesFrame.visibility = View.VISIBLE
                }
            }
        }
    }

}