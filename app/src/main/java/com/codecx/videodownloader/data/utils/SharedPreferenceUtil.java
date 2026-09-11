package com.codecx.videodownloader.data.utils;

import static com.codecx.videodownloader.data.utils.Constants.IS_ALL_ADS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_ALL_BANNER_ADS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_ALL_INTERSTITIAL_ADS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_ALL_NATIVE_ADS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_APP_OPEN_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_FIRST_TIME_APP_OPEN;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_PHOTO_ALBUM_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_PHOTO_VIEW_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_VIDEO_VIEW_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.LANGUAGE_PREFERENCE;
import static com.codecx.videodownloader.data.utils.Constants.PHOTO_VIEW_INTERSTITIAL_COUNT;
import static com.codecx.videodownloader.data.utils.Constants.IS_APP_OPEN_SPLASH_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_BANNER_PHOTO_ALBUM_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTEREST_ACTIVITY_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_AUDIO_ALBUM_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_HOME_VAULT_FILES_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_HOME_VAULT_PHOTO_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_HOME_VAULT_RECYCLE_BIN_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_HOME_VAULT_VIDEO_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_HOME_VAULT_WEB_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_INTEREST_ACTIVITY_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_LANGUAGE_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_ONBOARDING_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_OTHERS_ALBUM_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_SCAN_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_SPLASH_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_VAULT_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_INTERSTITIAL_VIDEO_ALBUM_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_LANGUAGE_ACTIVITY_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_BOTTOM_ONBOARDING_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_EXIT_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_HOME_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_LANGUAGE_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_SCAN_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_SETTINGS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_VAULT_HIDE_PHOTOS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_NATIVE_VIDEOS_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_ONBOARDING_ACTIVITY_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.IS_PREMIUM_ACTIVITY_ACTIVE;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_HOME_BOTTOM;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_HOME_TOP;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_ONBOARDING_TOP;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_REELS_BOTTOM;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_REELS_TOP;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_SETTINGS_BOTTOM;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_SETTINGS_TOP;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_SPLASH_BOTTOM;
import static com.codecx.videodownloader.data.utils.Constants.WHICH_AD_WANT_TO_SHOW_ON_SPLASH_TOP;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferenceUtil {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public SharedPreferenceUtil(Context context) {
        preferences= context.getSharedPreferences("remotePref", Context.MODE_PRIVATE);
        editor= preferences.edit();
    }
    public Boolean getAllAdsStatus() {
        return preferences.getBoolean(IS_ALL_ADS_ACTIVE,true);
    }

    public void setAllAdsStatus(Boolean value){
        editor.putBoolean(IS_ALL_ADS_ACTIVE,value);
        editor.apply();
    }

    public void  setSubPrice(String string, String price) {
        editor.putString(string, price);
        editor.apply();
    }

    public String getSubPrice( String value ) {
        return preferences.getString(value,"$9.00");
    }
    public void  setLifeTimePrice( String price) {
        editor.putString("lifeTimePrice", price);
        editor.apply();
    }

    public String getLifeTimePrice() {
        return preferences.getString("lifeTimePrice","$20.00");
    }
    public Boolean isSubScribe() {
        return preferences.getBoolean("isSubscribe",false);
    }

    public void setSubscription(Boolean value){
        editor.putBoolean("isSubscribe",value);
        editor.apply();
    }
    public Boolean isBilling() {
        return preferences.getBoolean("isBilling",false);
    }

    public void setBilling(Boolean value){
        editor.putBoolean("isBilling",value);
        editor.apply();
    }
     public Boolean getAllBannerAdsStatus() {
        return preferences.getBoolean(IS_ALL_BANNER_ADS_ACTIVE,true);
    }

    public void setAllBannerAdsStatus(Boolean value){
        editor.putBoolean(IS_ALL_BANNER_ADS_ACTIVE,value);
        editor.apply();
    }
    public Boolean getAllInterstitialAdsStatus() {
        return preferences.getBoolean(IS_ALL_INTERSTITIAL_ADS_ACTIVE,true);
    }

    public void setAllInterstitialAdsStatus(Boolean value){
        editor.putBoolean(IS_ALL_INTERSTITIAL_ADS_ACTIVE,value);
        editor.apply();
    }
     public Boolean getAllNativeAdsStatus() {
        return preferences.getBoolean(IS_ALL_NATIVE_ADS_ACTIVE,true);
    }

    public void setAllNativeAdsStatus(Boolean value){
        editor.putBoolean(IS_ALL_NATIVE_ADS_ACTIVE,value);
        editor.apply();
    }
     public Boolean getAppOpenAd() {
        return preferences.getBoolean(IS_APP_OPEN_ACTIVE,true);
    }

    public void setAppOpenAd(Boolean value){
        editor.putBoolean(IS_APP_OPEN_ACTIVE,value);
        editor.apply();
    }
    public int getPhotoInterstitialCount() {
        return preferences.getInt(PHOTO_VIEW_INTERSTITIAL_COUNT,3);
    }

    public void setPhotoInterstitialCount(Long value){
        editor.putInt(PHOTO_VIEW_INTERSTITIAL_COUNT, Math.toIntExact(value));
        editor.apply();
    }

    public boolean isPhotoViewInterstitialActive() {

        return preferences.getBoolean(IS_INTERSTITIAL_PHOTO_VIEW_ACTIVE,true);
    }
    public void setPhotoViewInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_PHOTO_VIEW_ACTIVE,value);
        editor.apply();
    }
    public boolean isVideoViewInterstitialActive() {

        return preferences.getBoolean(IS_INTERSTITIAL_VIDEO_VIEW_ACTIVE,true);
    }
    public void setVideoViewInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_VIDEO_VIEW_ACTIVE,value);
        editor.apply();
    }
    public boolean isFistTimeAppOpen() {

        return preferences.getBoolean(IS_FIRST_TIME_APP_OPEN,true);
    }
    public void setLanguagePreference(String value) {
        editor.putString(LANGUAGE_PREFERENCE, value);
        editor.apply();
    }

    public String getLanguagePreference() {
        return preferences.getString(LANGUAGE_PREFERENCE, "");
    }

    public void setFistTimeAppOpen(Boolean value){
        editor.putBoolean(IS_FIRST_TIME_APP_OPEN,value);
        editor.apply();
    }
    public boolean isPhotoAlbumInterstitialActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_PHOTO_ALBUM_ACTIVE,true);
    }
    public void setPhotoAlbumInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_PHOTO_ALBUM_ACTIVE,value);
        editor.apply();
    }
    public boolean isVideoAlbumInterstitialActive() {

        return preferences.getBoolean(IS_INTERSTITIAL_VIDEO_ALBUM_ACTIVE,true);
    }
    public void setVideoAlbumInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_VIDEO_ALBUM_ACTIVE,value);
        editor.apply();
    }
    public boolean isAudioAlbumInterstitialActive() {

        return preferences.getBoolean(IS_INTERSTITIAL_AUDIO_ALBUM_ACTIVE,true);
    }
    public void setAudioAlbumInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_AUDIO_ALBUM_ACTIVE,value);
        editor.apply();
    }
    public boolean isOthersAlbumInterstitialActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_OTHERS_ALBUM_ACTIVE,true);
    }
    public void setOthersAlbumInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_OTHERS_ALBUM_ACTIVE,value);
        editor.apply();
    }
   public boolean isInterstitialHomeVaultWebActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_HOME_VAULT_WEB_ACTIVE,true);
    }
    public void setInterstitialHomeVaultWebActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_HOME_VAULT_WEB_ACTIVE,value);
        editor.apply();
    }
   public boolean isInterstitialHomeVaultPhotoActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_HOME_VAULT_PHOTO_ACTIVE,true);
    }
    public void setInterstitialHomeVaultPhotoActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_HOME_VAULT_PHOTO_ACTIVE,value);
        editor.apply();
    }
   public boolean isInterstitialVaultActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_VAULT_ACTIVE,true);
    }
    public void setInterstitialVaultActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_VAULT_ACTIVE,value);
        editor.apply();
    }
   public boolean isInterstitialHomeVaultVideoActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_HOME_VAULT_VIDEO_ACTIVE,true);
    }
    public void setInterstitialHomeVaultVideoActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_HOME_VAULT_VIDEO_ACTIVE,value);
        editor.apply();
    }
   public boolean isInterstitialHomeVaultFilesActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_HOME_VAULT_FILES_ACTIVE,true);
    }
    public void setInterstitialHomeVaultFilesActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_HOME_VAULT_FILES_ACTIVE,value);
        editor.apply();
    }
   public boolean isInterstitialHomeVaultRecycleBinActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_HOME_VAULT_RECYCLE_BIN_ACTIVE,true);
    }
    public void setInterstitialHomeVaultRecycleBinActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_HOME_VAULT_RECYCLE_BIN_ACTIVE,value);
        editor.apply();
    }
    public boolean isScanInterstitialActive() {

        return preferences.getBoolean(IS_INTERSTITIAL_SCAN_ACTIVE,true);
    }
    public void setScanInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_SCAN_ACTIVE,value);
        editor.apply();
    }
    public boolean isSplashInterstitialActive() {

        return preferences.getBoolean(IS_INTERSTITIAL_SPLASH_ACTIVE,true);
    }
    public void setSplashInterstitialActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_SPLASH_ACTIVE,value);
        editor.apply();
    }
    public boolean getAppOpenAdSplash() {

        return preferences.getBoolean(IS_APP_OPEN_SPLASH_ACTIVE,true);
    }
    public void setAppOpenAdSplashActive(Boolean value){
        editor.putBoolean(IS_APP_OPEN_SPLASH_ACTIVE,value);
        editor.apply();
    }
    public boolean isHomeNativeActive() {

        return preferences.getBoolean(IS_NATIVE_HOME_ACTIVE,true);
    }
    public void setHomeNativeActive(Boolean value){
        editor.putBoolean(IS_NATIVE_HOME_ACTIVE,value);
        editor.apply();
    }
    public boolean isExitNativeActive() {

        return preferences.getBoolean(IS_NATIVE_EXIT_ACTIVE,true);
    }
    public void setExitNativeActive(Boolean value){
        editor.putBoolean(IS_NATIVE_EXIT_ACTIVE,value);
        editor.apply();
    }
    public boolean isVideoNativeActive() {

        return preferences.getBoolean(IS_NATIVE_VIDEOS_ACTIVE,true);
    }
    public void setVideoNativeActive(Boolean value){
        editor.putBoolean(IS_NATIVE_VIDEOS_ACTIVE,value);
        editor.apply();
    }
    public boolean isScanNativeActive() {

    return preferences.getBoolean(IS_NATIVE_SCAN_ACTIVE,true);
    }
    public void setScanNativeActive(Boolean value){
        editor.putBoolean(IS_NATIVE_SCAN_ACTIVE,value);
        editor.apply();
    }
    public boolean isSettingsNativeActive() {
        return preferences.getBoolean(IS_NATIVE_SETTINGS_ACTIVE,true);
    }
    public void setSettingsNativeActive(Boolean value){
        editor.putBoolean(IS_NATIVE_SETTINGS_ACTIVE,value);
        editor.apply();
    }
    public boolean isNativeVaultHidePhotosActive() {
        return preferences.getBoolean(IS_NATIVE_VAULT_HIDE_PHOTOS_ACTIVE,true);
    }
    public void setNativeVaultHidePhotosActive(Boolean value){
        editor.putBoolean(IS_NATIVE_VAULT_HIDE_PHOTOS_ACTIVE,value);
        editor.apply();
    }
    public boolean isNativeBottomOnboardingActive() {
        return preferences.getBoolean(IS_NATIVE_BOTTOM_ONBOARDING_ACTIVE,true);
    }
    public void setNativeBottomOnboardingActive(Boolean value){
        editor.putBoolean(IS_NATIVE_BOTTOM_ONBOARDING_ACTIVE,value);
        editor.apply();
    }
    public boolean isPhotoAlbumBannerActive() {
        return preferences.getBoolean(IS_BANNER_PHOTO_ALBUM_ACTIVE,true);
    }
    public void setPhotoAlbumBannerActive(Boolean value){
        editor.putBoolean(IS_BANNER_PHOTO_ALBUM_ACTIVE,value);
        editor.apply();
    }
    public boolean isInterstitialLanguageActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_LANGUAGE_ACTIVE,true);
    }
    public void setInterstitialLanguageActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_LANGUAGE_ACTIVE,value);
        editor.apply();
    }
    public boolean isInterstitialInterestActivityActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_INTEREST_ACTIVITY_ACTIVE,true);
    }
    public void setInterstitialInterestActivityActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_INTEREST_ACTIVITY_ACTIVE,value);
        editor.apply();
    }
    public boolean isInterstitialOnboardingActive() {
        return preferences.getBoolean(IS_INTERSTITIAL_ONBOARDING_ACTIVE,true);
    }
    public void setInterstitialOnboardingActive(Boolean value){
        editor.putBoolean(IS_INTERSTITIAL_ONBOARDING_ACTIVE,value);
        editor.apply();
    }
    public boolean isInterestActivityActive(){
       return preferences.getBoolean(IS_INTEREST_ACTIVITY_ACTIVE,true);
    }
    public void setInterestActivityActive(Boolean value){
        editor.putBoolean(IS_INTEREST_ACTIVITY_ACTIVE,value);
        editor.apply();
    }
    public boolean isPremiumActivityActive(){
       return preferences.getBoolean(IS_PREMIUM_ACTIVITY_ACTIVE,true);
    }
    public void setPremiumActivityActive(Boolean value){
        editor.putBoolean(IS_PREMIUM_ACTIVITY_ACTIVE,value);
        editor.apply();
    }
    public boolean isOnboardingActivityActive() {
        return preferences.getBoolean(IS_ONBOARDING_ACTIVITY_ACTIVE,true);
    }
    public void setOnboardingActivityActive(Boolean value){
        editor.putBoolean(IS_ONBOARDING_ACTIVITY_ACTIVE,value);
        editor.apply();
    }
    public boolean isNativeLanguageActive() {
        return preferences.getBoolean(IS_NATIVE_LANGUAGE_ACTIVE,true);
    }
    public void setNativeLanguageActive(Boolean value){
        editor.putBoolean(IS_NATIVE_LANGUAGE_ACTIVE,value);
        editor.apply();
    }
    public boolean isLanguageActivityActive() {
        return preferences.getBoolean(IS_LANGUAGE_ACTIVITY_ACTIVE,true);
    }
    public void setLanguageActivityActive(Boolean value){
        editor.putBoolean(IS_LANGUAGE_ACTIVITY_ACTIVE,value);
        editor.apply();
    }


    public String getWhichAdWantToShowOnOnboardingTop() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_ONBOARDING_TOP,"banner");
    }
    public void setWhichAdWantToShowOnOnboardingTop(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_ONBOARDING_TOP,value);
        editor.apply();
    }

    public String getWhichAdWantToShowOnHomeTop() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_HOME_TOP,"banner");
    }
    public void setWhichAdWantToShowOnHomeTop(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_HOME_TOP,value);
        editor.apply();
    }


    public String getWhichAdWantToShowOnHomeBottom() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_HOME_BOTTOM,"native");
    }
    public void setWhichAdWantToShowOnHomeBottom(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_HOME_BOTTOM,value);
        editor.apply();
    }


    public String getWhichAdWantToShowOnReelsTop() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_REELS_TOP,"banner");
    }
    public void setWhichAdWantToShowOnReelsTop(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_REELS_TOP,value);
        editor.apply();
    }

    public String getWhichAdWantToShowOnReelsBottom() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_REELS_BOTTOM,"native");
    }
    public void setWhichAdWantToShowOnReelsBottom(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_REELS_BOTTOM,value);
        editor.apply();
    }

    public String getWhichAdWantToShowOnSettingsTop() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_SETTINGS_TOP,"banner");
    }
    public void setWhichAdWantToShowOnSettingsTop(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_SETTINGS_TOP,value);
        editor.apply();
    }

    public String getWhichAdWantToShowOnSettingsBottom() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_SETTINGS_BOTTOM,"native");
    }
    public void setWhichAdWantToShowOnSettingsBottom(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_SETTINGS_BOTTOM,value);
        editor.apply();
    }

    public String getWhichAdWantToShowOnSplashTop() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_SPLASH_TOP,"banner");
    }
    public void setWhichAdWantToShowOnSplashTop(String value){
        editor.putString(WHICH_AD_WANT_TO_SHOW_ON_SPLASH_TOP,value);
        editor.apply();
    }

    public String getWhichAdWantToShowOnSplashBottom() {
        return preferences.getString(WHICH_AD_WANT_TO_SHOW_ON_SPLASH_BOTTOM,"native");
    }
    public void getWhichAdWantToShowOnSplashBottom(String value){
        editor.putString(Constants.WHICH_AD_WANT_TO_SHOW_ON_SPLASH_BOTTOM,value);
        editor.apply();
    }

}
