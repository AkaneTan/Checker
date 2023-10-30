package org.akanework.checker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT
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

    private fun changeTitleStatus(status: Int) {
        var titleString = getString(R.string.normal_title)
        var summaryString = getString(R.string.normal_summary)
        var targetColor: Int
        var targetTextColor: Int

        CoroutineScope(Dispatchers.Default).launch {
            when (status) {
                1 -> {
                    targetColor = MaterialColors.harmonizeWithPrimary(
                        this@MainActivity,
                        getColor(R.color.green)
                    )
                    targetTextColor = MaterialColors.harmonizeWithPrimary(
                        this@MainActivity,
                        getColor(R.color.colorOnGreen)
                    )
                }

                2 -> {
                    titleString = getString(R.string.notice_title)
                    summaryString = getString(R.string.notice_summary)
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
        if (Build.VERSION.SDK_INT >= 26) {
            window.colorMode = COLOR_MODE_WIDE_COLOR_GAMUT
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

        val abnormalitiesFrame = findViewById<MaterialCardView>(R.id.abnormal_frame)
        val abnormalitiesRecyclerView = findViewById<RecyclerView>(R.id.abnormal_recyclerview)
        abnormalitiesRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        abnormalitiesRecyclerView.adapter = abnormalitiesAdapter

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
        val widevineKeyDrm = MediaDrm(WIDEVINE_UUID)
        val drmLevel = widevineKeyDrm.getPropertyString("securityLevel")
        val drmVersion = widevineKeyDrm.getPropertyString(PROPERTY_VERSION)
        val drmVendorLevel = widevineKeyDrm.getPropertyString(PROPERTY_VENDOR)
        val drmDesc = widevineKeyDrm.getPropertyString(PROPERTY_DESCRIPTION)
        val drmAlgo = widevineKeyDrm.getPropertyString(PROPERTY_ALGORITHMS)

        widevineLevelTextView.text = "${getString(R.string.drm_security_level)} - $drmLevel"
        widevineVersionLevelTextView.text = "${getString(R.string.drm_version)} - $drmVersion"
        widevineVendorLevelTextView.text = "${getString(R.string.drm_vendor)} - $drmVendorLevel"
        widevineDescTextView.text = "${getString(R.string.drm_desc)} - $drmDesc"
        widevineAlgoTextView.text = "${getString(R.string.drm_algo)} - $drmAlgo"

        // Set up radio info
        val connectivityRadioVersion = Build.getRadioVersion().substringAfterLast(',')
        val connectivityGNSSQueryJob = CoroutineScope(Dispatchers.Default).async {
            val connectivityGNSSVersionRawList = CheckerUtils.checkGnssHal()
            val connectivityGNSSVersionList = connectivityGNSSVersionRawList
                .filter { it.contains("android.hardware.gnss") }
                .map {
                    it.substringBefore(":").substringAfterLast("? ").substringAfterLast("Y ").trim()
                }
                .distinct()
                .joinToString(separator = "\n")
            if (connectivityGNSSVersionRawList.isNotEmpty()) {
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
                    abnormalitiesList.add(getString(R.string.abnormalities_gnss_hal_broken))
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
        val mediaIsDeviceColorGamut =
            if (Build.VERSION.SDK_INT >= 26) ColorSpace.get(ColorSpace.Named.SRGB).isWideGamut else false

        mediaHdrTypeTextView.text =
            "${getString(R.string.media_supported_hdr_types)} - $mediaHdrString"
        mediaWideColorGamutTextView.text =
            "${getString(R.string.media_wide_color_gamut)} - $mediaIsDeviceColorGamut"

        // Set up security info
        val securitySELinuxQueryJob = CoroutineScope(Dispatchers.Default).async {
            val securityIsSELinuxEnforcing = CheckerUtils.isSELinuxEnforcing()
            withContext(Dispatchers.Main) {
                securitySELinuxStateTextView.text = getString(R.string.security_selinux_state) + " - " +
                    when (securityIsSELinuxEnforcing) {
                        0 -> "Enforcing"
                        1 -> {
                            abnormalitiesList.add(getString(R.string.abnormalities_selinux_not_enforcing))
                            "Permissive"
                        }
                        2 -> {
                            "Invalid"
                        }
                        else -> throw IllegalArgumentException()
                    }
            }
        }

        // Get abnormalities
        val verifiedBootStat = SystemProperties.read("ro.boot.verifiedbootstate", "unknown")
        val isDrmPassing =
            if (drmLevel == "L1") 0 else if (drmLevel == "L2") 1 else if (drmLevel == "L3") 2 else 3

        if (isDrmPassing != 0) {
            abnormalitiesList.add(getString(R.string.abnormalities_widevine))
        }
        if (verifiedBootStat != "green") {
            abnormalitiesList.add(getString(R.string.abnormalities_verified_boot_stat))
        }
        if (connectivityRadioVersion == "unknown") {
            abnormalitiesList.add(getString(R.string.abnormalities_baseband_broken))
        }

        CoroutineScope(Dispatchers.Default).launch {
            awaitAll(connectivityGNSSQueryJob, securitySELinuxQueryJob)
            withContext(Dispatchers.Main) {
                if (abnormalitiesList.isEmpty()) {
                    changeTitleStatus(1)
                } else {
                    changeTitleStatus(2)
                }
                if (abnormalitiesList.size != 0) {
                    abnormalitiesAdapter.notifyItemRangeInserted(0, abnormalitiesList.size)
                    abnormalitiesFrame.visibility = View.VISIBLE
                }
            }
        }
    }

}