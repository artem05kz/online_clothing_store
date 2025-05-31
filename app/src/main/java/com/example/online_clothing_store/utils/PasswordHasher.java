package com.example.online_clothing_store.utils;

import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean check(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            Log.e("PasswordHasher", "Error checking password", e);
            return false;
        }
    }
}