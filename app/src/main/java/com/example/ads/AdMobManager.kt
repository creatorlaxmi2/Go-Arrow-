package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"
    
    // User provided rewarded ad unit ID & Google test ad unit IDs
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3625054367842730/9243313584"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    
    // Banner ad unit ID (using test or user provided)
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
               android.os.Build.MODEL.contains("google_sdk") ||
               android.os.Build.HARDWARE.contains("goldfish") ||
               android.os.Build.HARDWARE.contains("ranchu") ||
               android.os.Build.PRODUCT.contains("sdk")
    }

    fun initialize(context: Context) {
        try {
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialized: $initializationStatus")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob", e)
        }
    }

    fun loadRewardedAd(context: Context) {
        // Skip heavy video decoder preloads in emulator environments to avoid media codec errors
        if (isEmulator() || isAdLoading || rewardedAd != null) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitToUse = if (com.example.BuildConfig.DEBUG || REWARDED_AD_UNIT_ID.endsWith("9243313584")) {
            TEST_REWARDED_AD_UNIT_ID
        } else {
            REWARDED_AD_UNIT_ID
        }
        
        try {
            RewardedAd.load(
                context,
                adUnitToUse,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isAdLoading = false
                        Log.d(TAG, "Rewarded ad loaded successfully.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isAdLoading = false
                        Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isAdLoading = false
            Log.w(TAG, "Error initiating rewarded ad load", e)
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissedOrFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            try {
                ad.show(activity) { rewardItem ->
                    Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    onRewardEarned()
                    rewardedAd = null
                    loadRewardedAd(activity)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show rewarded ad", e)
                onRewardEarned()
                rewardedAd = null
            }
        } else {
            Log.d(TAG, "Rewarded ad not loaded or emulator mode. Granting fallback reward.")
            onRewardEarned()
            loadRewardedAd(activity)
        }
    }
}

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    if (AdMobManager.isEmulator()) {
        // Skip mounting Chromium WebView in headless container emulator to prevent hardware codec / rendernode queries
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobManager.BANNER_AD_UNIT_ID
                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        Log.w("AdMobManager", "Failed to load banner ad", e)
                    }
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (_: Exception) {}
            }
        )
    }
}
