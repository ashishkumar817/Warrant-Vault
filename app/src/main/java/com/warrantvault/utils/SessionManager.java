package com.warrantvault.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "warrant_vault_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    // Reminder preferences
    public static final String KEY_REMIND_30 = "remind_30";
    public static final String KEY_REMIND_14 = "remind_14";
    public static final String KEY_REMIND_7 = "remind_7";
    public static final String KEY_REMIND_1 = "remind_1";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(long userId, String name, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name != null ? name : "User");
        editor.putString(KEY_USER_EMAIL, email != null ? email : "");
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void setOnboardingCompleted(boolean completed) {
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, completed);
        editor.apply();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    public boolean isRemind30() {
        return prefs.getBoolean(KEY_REMIND_30, true);
    }

    public void setRemind30(boolean enabled) {
        editor.putBoolean(KEY_REMIND_30, enabled);
        editor.apply();
    }

    public boolean isRemind14() {
        return prefs.getBoolean(KEY_REMIND_14, true);
    }

    public void setRemind14(boolean enabled) {
        editor.putBoolean(KEY_REMIND_14, enabled);
        editor.apply();
    }

    public boolean isRemind7() {
        return prefs.getBoolean(KEY_REMIND_7, true);
    }

    public void setRemind7(boolean enabled) {
        editor.putBoolean(KEY_REMIND_7, enabled);
        editor.apply();
    }

    public boolean isRemind1() {
        return prefs.getBoolean(KEY_REMIND_1, true);
    }

    public void setRemind1(boolean enabled) {
        editor.putBoolean(KEY_REMIND_1, enabled);
        editor.apply();
    }

    public void logout() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }
}
