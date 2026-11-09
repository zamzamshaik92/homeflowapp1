package com.example.ads

import com.example.BuildConfig

/**
 * Production Google AdMob configuration.
 * All credentials are read dynamically from BuildConfig (injected via Secrets panel / .env).
 * No Google test ad IDs remain in the codebase.
 */
object AdConfig {
    val APP_ID: String
        get() = try { BuildConfig.ADMOB_APP_ID.trim() } catch (_: Throwable) { "" }

    val BANNER_AD_UNIT_ID: String
        get() = try { BuildConfig.ADMOB_BANNER_AD_UNIT_ID.trim() } catch (_: Throwable) { "" }

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = try { BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID.trim() } catch (_: Throwable) { "" }

    val isAppIdConfigured: Boolean
        get() = APP_ID.isNotBlank() && APP_ID.startsWith("ca-app-pub-")

    val isBannerConfigured: Boolean
        get() = isAppIdConfigured && BANNER_AD_UNIT_ID.isNotBlank() && BANNER_AD_UNIT_ID.startsWith("ca-app-pub-")

    val isInterstitialConfigured: Boolean
        get() = isAppIdConfigured && INTERSTITIAL_AD_UNIT_ID.isNotBlank() && INTERSTITIAL_AD_UNIT_ID.startsWith("ca-app-pub-")
}
