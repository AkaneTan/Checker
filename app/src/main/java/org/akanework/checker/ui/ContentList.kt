package org.akanework.checker.ui

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.akanework.checker.MainActivity
import org.akanework.checker.R
import org.akanework.checker.detection.CheckerMethods
import org.akanework.checker.model.CheckerInfo
import org.akanework.checker.model.CheckerInfoItem
import org.akanework.checker.ui.components.InfoCard
import org.akanework.checker.ui.components.StatusCard

@SuppressLint("SetTextI18n", "HardwareIds")
@Composable
fun ContentList(
    innerPadding: PaddingValues,
    contentResolver: ContentResolver,
    activity: MainActivity
) {
    val abnormalitiesList = remember { mutableStateListOf<String>() }
    var securityLevel by remember { mutableIntStateOf(0) }

    val architectureInfo = CheckerInfo(
        stringResource(id = R.string.architecture),
        R.drawable.ic_memory_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.architecture_soc_model),
                if (Build.VERSION.SDK_INT >= 31) {
                    Build.SOC_MODEL
                } else {
                    stringResource(id = R.string.architecture_failed_to_retrieve_soc)
                }
            ),
            CheckerInfoItem(
                stringResource(id = R.string.architecture_supported_abi),
                Build.SUPPORTED_ABIS.joinToString(separator = ", ")
            )
        )
    )

    val abnormalitiesListInfo = CheckerInfo(
        stringResource(id = R.string.abnormalities),
        R.drawable.ic_error_outline_24dp,
        listOf(
            CheckerInfoItem
                (
                abnormalitiesList.toList().joinToString(separator = "\n· ", prefix = "· "),
                ""
            )
        )
    )

    val basicInfo = CheckerInfo(
        stringResource(id = R.string.basic),
        R.drawable.ic_info_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.basic_android_version),
                Build.VERSION.RELEASE,
                true
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_android_sdk),
                Build.VERSION.SDK_INT.toString(),
                true
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_android_id),
                Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_brand),
                Build.BRAND
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_model),
                Build.MODEL
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_model_sku),
                if (Build.VERSION.SDK_INT >= 31) Build.SKU else ""
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_build_type),
                Build.TYPE
            ),
            CheckerInfoItem(
                stringResource(id = R.string.basic_build_fingerprint),
                Build.FINGERPRINT
            )
        )
    )
    val connectivityInfo = CheckerInfo(
        stringResource(id = R.string.connectivity),
        R.drawable.ic_signal_cellular_alt_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.connectivity_radio),
                Build.getRadioVersion().substringAfterLast(',').also {
                    if( it == "unknown" ) {
                        if (!abnormalitiesList.contains(
                                stringResource(id = R.string.abnormalities_baseband_broken)
                        )) {
                            abnormalitiesList.add(
                                stringResource(id = R.string.abnormalities_baseband_broken)
                            )
                        }
                        securityLevel = 2
                    }
                }
            ),
            CheckerInfoItem(
                stringResource(id = R.string.connectivity_gnss),
                CheckerMethods.getGnssHidlHalList()
            )
        )
    )
    val mediaInfo = CheckerInfo(
        stringResource(id = R.string.media),
        R.drawable.ic_image_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.media_supported_hdr_types),
                CheckerMethods.getMediaHdrList(activity)
            ),
            CheckerInfoItem(
                stringResource(id = R.string.media_wide_color_gamut),
                if (Build.VERSION.SDK_INT >= 26 && Configuration().isScreenWideColorGamut)
                    stringResource(id = R.string.media_window_available)
                else
                    stringResource(id = R.string.media_window_unavailable)
            )
        )
    )
    val securityInfo = CheckerInfo(
        stringResource(id = R.string.security),
        R.drawable.ic_lock_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.security_selinux_state),
                when(CheckerMethods.getSelinuxStatus()){
                    0 -> "Enforcing"
                    1 -> {
                        if (!abnormalitiesList.contains(
                                stringResource(id = R.string.abnormalities_selinux_not_enforcing)
                            )) {
                            abnormalitiesList.add(
                                stringResource(id = R.string.abnormalities_selinux_not_enforcing)
                            )
                        }
                        securityLevel = 2
                        "Permissive"
                    }

                    2 -> "Invalid"

                    else -> throw IllegalArgumentException()
                },
                true
            ),
            CheckerInfoItem(
                stringResource(id = R.string.security_verified_boot_state),
                CheckerMethods.getVerifiedBootState().also {
                    if (it != "green") {
                        if (!abnormalitiesList.contains(
                                stringResource(id = R.string.abnormalities_verified_boot_stat)
                            )) {
                            abnormalitiesList.add(
                                stringResource(id = R.string.abnormalities_verified_boot_stat)
                            )
                        }
                        if (securityLevel == 0) securityLevel = 1
                    }
                },
                true
            ),
            CheckerInfoItem(
                stringResource(id = R.string.security_signing_key),
                CheckerMethods.getSystemSignKey().also {
                    when (it) {
                        "test-keys" -> {
                            if (!abnormalitiesList.contains(
                                    stringResource(
                                        id = R.string.abnormalities_signed_using_a_publickey
                                    )
                                )) {
                                abnormalitiesList.add(
                                    stringResource(
                                        id = R.string.abnormalities_signed_using_a_publickey
                                    )
                                )
                            }
                            if (securityLevel == 0) securityLevel = 1
                        }
                        CheckerMethods.INVALID -> {
                            if (!abnormalitiesList.contains(
                                    stringResource(
                                        id = R.string.abnormalities_undefined_signing_key
                                    )
                                )
                            ) {
                                abnormalitiesList.add(
                                    stringResource(
                                        id = R.string.abnormalities_undefined_signing_key
                                    )
                                )
                            }
                            securityLevel = 2
                        }
                    }
                }
            )
        )
    )
    val universalityInfo = CheckerInfo(
        stringResource(id = R.string.universality),
        R.drawable.ic_hexagon_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.universality_treble_support),
                CheckerMethods.getTrebleEnabledState().toString(),
                true
            ),
            CheckerInfoItem(
                stringResource(id = R.string.universality_gsi_compatibility),
                if(CheckerMethods.getTrebleEnabledState()) {
                    stringResource(id = R.string.available) + " (Treble)"
                } else {
                    stringResource(id = R.string.unavailable)
                }
            ),
            CheckerInfoItem(
                stringResource(id = R.string.universality_dsu_status),
                if(Build.VERSION.SDK_INT >= 31) {
                    stringResource(id = R.string.available)
                } else {
                    stringResource(id = R.string.unavailable)
                }
            )
        )
    )
    val widevineInfo = CheckerInfo(
        stringResource(id = R.string.widevine),
        R.drawable.ic_widevine_24dp,
        listOf(
            CheckerInfoItem(
                stringResource(id = R.string.drm_security_level),
                CheckerMethods.getDrmInfo().drmLevel.also {
                    if ( it != "L1" ) {
                        if (!abnormalitiesList.contains(
                                stringResource(
                                    id = R.string.abnormalities_widevine
                                )
                            )
                        ) {
                            abnormalitiesList.add(
                                stringResource(
                                    id = R.string.abnormalities_widevine
                                )
                            )
                        }
                        if (securityLevel == 0) securityLevel = 1
                    }
                },
                true
            ),
            CheckerInfoItem(
                stringResource(id = R.string.drm_version),
                CheckerMethods.getDrmInfo().drmVersion
            ),
            CheckerInfoItem(
                stringResource(id = R.string.drm_vendor),
                CheckerMethods.getDrmInfo().drmVendorLevel
            ),
            CheckerInfoItem(
                stringResource(id = R.string.drm_desc),
                CheckerMethods.getDrmInfo().drmDesc
            ),
            CheckerInfoItem(
                stringResource(id = R.string.drm_algo),
                CheckerMethods.getDrmInfo().drmAlgo
            )
        )
    )

    Column(
        androidx.compose.ui.Modifier
            .padding(innerPadding)
    ) {
        StatusCard(securityLevel)
        if (!abnormalitiesList.isEmpty()) {
            InfoCard(abnormalitiesListInfo)
        }
        InfoCard(architectureInfo)
        InfoCard(basicInfo)
        InfoCard(connectivityInfo)
        InfoCard(mediaInfo)
        InfoCard(securityInfo)
        InfoCard(universalityInfo)
        InfoCard(widevineInfo)
    }
}