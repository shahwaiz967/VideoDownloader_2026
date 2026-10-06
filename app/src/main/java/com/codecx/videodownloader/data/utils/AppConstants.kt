package com.codecx.videodownloader.data.utils

object AppConstants {
    const val REEL_ACCESS_TOKEN: String =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZXN1bHQiOnsiRmlyc3ROYW1lIjoiZG93bmxvYWRlcl9uZXciLCJMYXN0TmFtZSI6ImRvd25sb2FkZXJfbmV3IiwiVXNlcm5hbWUiOiJkb3dubG9hZGVyX25ldyIsImVtYWlsIjoiZG93bmxvYWRlcl9uZXdAZ21haWwuY29tIn0sImlhdCI6MTY5ODEyNzc0OX0.496rh0QGPsWM0X2LMBlmDZ6hU6Dd9BYdtfQg_pzgLdo"
    const val REEL_BASE_URL: String = "https://downloadvid.app/"
    // ─── API ───────────────────────────────────────────────────────────────
    const val BASE_URL = "https://auto-download-all-in-one.p.rapidapi.com"
    const val API_ENDPOINT = "/v1/social/autolink"
    const val RAPID_API_KEY = "962fb54da7msha2de57f1dc76f79p16c4eejsn478e0c757994"
    const val RAPID_API_HOST = "auto-download-all-in-one.p.rapidapi.com"

    // ─── Preferences Keys ─────────────────────────────────────────────────
    const val PREF_ONBOARDING_DONE = "onboarding_done"
    const val PREF_LANGUAGE_DONE = "language_done"
    const val PREF_INTEREST_DONE = "interest_done"
    const val PREF_PREMIUM_DONE = "premium_done"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_LANGUAGE = "selected_language"
    const val PREF_DEFAULT_BROWSER = "default_browser"
    const val PREF_SEARCH_ENGINE = "search_engine"

    // ─── Download Folder ──────────────────────────────────────────────────
    const val DOWNLOAD_FOLDER = "VideoDownloader"

    // ─── Notification ─────────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID = "download_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Downloads"
    const val NOTIFICATION_ID = 1001

    // ─── Platforms ────────────────────────────────────────────────────────
    const val PLATFORM_ALL = "All"
    const val PLATFORM_FACEBOOK = "Facebook"
    const val PLATFORM_INSTAGRAM = "Instagram"
    const val PLATFORM_TIKTOK = "TikTok"

    // ─── Subscriptions ───────────────────────────────────────────────────
    const val SUB_PREMIUM_WEEKLY = "premium_weekly"
    const val SUB_PREMIUM_WEEKLY_DISCOUNT = "premium_weekly_discount"
    const val SUB_PREMIUM_MONTHLY = "premium_monthly"
    const val SUB_PREMIUM_MONTHLY_DISCOUNT = "premium_monthly_discount"
    const val SUB_PREMIUM_3_MONTHS = "premium_3_months"
    const val SUB_PREMIUM_3_MONTHS_DISCOUNT = "premium_3_months_discount"
    const val SUB_PREMIUM_SPECIAL_OFFER = "premium_special_offer"
    const val SUB_PREMIUM_WEEKLY_3DAY_TRIAL = "premium_weekly_3day_trial"
    const val SUB_PREMIUM_WEEKLY_GIFT = "premium_weekly_gift"

    val ALL_SUBSCRIPTION_IDS = listOf(
        SUB_PREMIUM_WEEKLY,
        SUB_PREMIUM_WEEKLY_DISCOUNT,
        SUB_PREMIUM_MONTHLY,
        SUB_PREMIUM_MONTHLY_DISCOUNT,
        SUB_PREMIUM_3_MONTHS,
        SUB_PREMIUM_3_MONTHS_DISCOUNT,
        SUB_PREMIUM_SPECIAL_OFFER,
        SUB_PREMIUM_WEEKLY_3DAY_TRIAL,
        SUB_PREMIUM_WEEKLY_GIFT
    )

    // ─── Intent Keys ──────────────────────────────────────────────────────
    const val INTENT_VIDEO_URI = "video_uri"
    const val INTENT_VIDEO_TITLE = "video_title"
    const val INTENT_IS_AUDIO = "is_audio"
}
