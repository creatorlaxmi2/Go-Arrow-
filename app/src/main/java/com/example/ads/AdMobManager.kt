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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    private const val TAG = "AdMobManager"
    
    // User provided rewarded ad unit ID & Google test ad unit IDs
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3625054367842730/9243313584"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    
    // Banner ad unit ID (using test or user provided)
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialized: $initializationStatus")
                loadRewardedAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob", e)
        }
    }

    fun loadRewardedAd(context: Context) {
        if (isAdLoading || rewardedAd != null) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        
        // Try user provided ID first, fallback to test ID if needed
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load with primary ID: ${loadAdError.message}. Trying test ID...")
                    // Fallback to test ID
                    RewardedAd.load(
                        context,
                        TEST_REWARDED_AD_UNIT_ID,
                        adRequest,
                        object : RewardedAdLoadCallback() {
                            override fun onAdLoaded(testAd: RewardedAd) {
                                rewardedAd = testAd
                                isAdLoading = false
                                Log.d(TAG, "Rewarded ad loaded successfully with test ID.")
                            }
                            override fun onAdFailedToLoad(testError: LoadAdError) {
                                rewardedAd = null
                                isAdLoading = false
                                Log.e(TAG, "Rewarded ad failed to load with test ID: ${testError.message}")
                            }
                        }
                    )
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissedOrFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
                rewardedAd = null
                loadRewardedAd(activity)
            }
        } else {
            Log.d(TAG, "Rewarded ad was not ready yet. Granting fallback reward for great user experience.")
            // Graceful fallback for seamless gameplay if ad network is offline/slow
            onRewardEarned()
            loadRewardedAd(activity)
        }
    }
}

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
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
                    adUnitId = "ca-app-pub-3940256099942544/6300978111" // Standard test banner or production ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
