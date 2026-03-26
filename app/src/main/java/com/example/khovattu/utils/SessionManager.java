package com.example.khovattu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class SessionManager {

    private static final String PREF_NAME = "khovattu_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_MANV = "maNV";   // ⭐ Lưu mã nhân viên

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ============================================================
    // 🔹 Lưu login đầy đủ
    // ============================================================
    public void saveLoginFull(String token, String username, String email, String role, String maNV) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_MANV, maNV);   // ⭐ Lưu maNV
        editor.apply();
    }

    // ============================================================
    // 🔹 Lưu maNV riêng (phòng backend trả về sau)
    // ============================================================
    public void saveMaNV(String maNV) {
        editor.putString(KEY_MANV, maNV);
        editor.apply();
    }

    public String getMaNV() {
        return prefs.getString(KEY_MANV, "");
    }

    // ============================================================
    // 🔹 Token
    // ============================================================
    public String getRawToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getToken() {
        String token = getRawToken();
        if (token == null || token.isEmpty()) return "";

        token = token.trim();
        if (token.startsWith("Bearer ")) return token;

        return "Bearer " + token;
    }

    // ============================================================
    // 🔹 User Info
    // ============================================================
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        String role = prefs.getString(KEY_ROLE, "user");
        if (role == null) return "user";
        return role.trim().toLowerCase();
    }

    // ============================================================
    // 🔹 Kiểm tra login
    // ============================================================
    public boolean isLoggedIn() {
        return !getRawToken().isEmpty();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // ============================================================
    // 🔹 Kiểm tra Token hết hạn
    // ============================================================
    public boolean isTokenExpired() {
        try {
            String token = getRawToken();
            if (token == null || token.isEmpty()) return true;

            String[] parts = token.split("\\.");
            if (parts.length < 2) return true;

            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);

            JSONObject json = new JSONObject(payload);
            long exp = json.optLong("exp", 0);

            if (exp == 0) return false;

            long now = System.currentTimeMillis() / 1000;
            return exp < now;

        } catch (Exception e) {
            Log.e("SessionManager", "Lỗi kiểm tra hạn token: " + e.getMessage());
            return true;
        }
    }

    public boolean isTokenValid() {
        return isLoggedIn() && !isTokenExpired();
    }
}
