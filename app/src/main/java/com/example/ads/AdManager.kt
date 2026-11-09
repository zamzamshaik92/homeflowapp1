package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages Google Mobile Ads SDK initialization and Interstitial Ad lifecycle for production.
 * Gracefully disables ads when credentials are not configured in Secrets.
 */
object AdManager {
    private const val TAG = "AdManager"

    private val isInitialized = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    /**
     * Initializes the Mobile Ads SDK when a valid production App ID is configured.
     */
    fun initialize(context: Context) {
        if (!AdConfig.isAppIdConfigured) {
            Log.i(TAG, "AdMob App ID is not configured. Ads are disabled until ADMOB_APP_ID is set in Secrets.")
            return
        }

        if (isInitialized.compareAndSet(false, true)) {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "Google Mobile Ads SDK Initialized: $status")
                    loadInterstitial(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Mobile Ads SDK", e)
            }
        }
    }

    /**
     * Preloads an Interstitial Ad in the background when production credentials are configured.
     */
    fun loadInterstitial(context: Context) {
        if (!AdConfig.isInterstitialConfigured || interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Production Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "Production Interstitial ad failed to load: ${loadAdError.message} (code ${loadAdError.code})")
                }
            }
        )
    }

    /**
     * Displays an interstitial ad at a natural transition point.
     * Always invokes [onAdFinished] so the user experience is never blocked.
     */
    fun showInterstitial(activity: Activity, onAdFinished: () -> Unit) {
        if (!AdConfig.isInterstitialConfigured) {
            onAdFinished()
            return
        }

        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed by user.")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdFinished()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdFinished()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad is showing.")
                }
            }
            currentAd.show(activity)
        } else {
            loadInterstitial(activity)
            onAdFinished()
        }
    }
}
