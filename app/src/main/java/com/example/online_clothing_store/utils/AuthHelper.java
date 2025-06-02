package com.example.online_clothing_store.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.online_clothing_store.database.AppDatabase;
import com.example.online_clothing_store.database.entities.User;

public class AuthHelper {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    public static void saveCredentials(Context context, String email, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public static String[] getCredentials(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);
        String password = prefs.getString(KEY_PASSWORD, null);
        return new String[]{email, password};
    }
    public static User tryAutoLogin(Context context) {
        String[] creds = getCredentials(context);
        String email = creds[0];
        String password = creds[1];
        if (email == null || password == null) return null;

        AppDatabase db = AppDatabase.getInstance(context);
        User user = db.userDao().getUserByEmail(email);
        if (user != null && PasswordHasher.check(password, user.getPasswordHash())) {
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("user_id", user.getId()).apply();
            return user;
        }
        return null;
    }
}