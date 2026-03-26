package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String message;
    private String token;

    @SerializedName("user")
    private UserData user;

    public static class UserData {

        @SerializedName("id")
        private String id;

        @SerializedName("username")
        private String username;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("maNV")
        private String maNV;

        // ===== GETTER =====
        public String getId() { return id; }

        public String getUsername() { return username; }

        public String getEmail() { return email; }

        public String getRole() { return role != null ? role.toLowerCase() : "user"; }

        public String getMaNV() { return maNV; }   // ⭐ QUAN TRỌNG

        // ===== SETTER (không bắt buộc) =====
        public void setId(String id) { this.id = id; }

        public void setUsername(String username) { this.username = username; }

        public void setEmail(String email) { this.email = email; }

        public void setRole(String role) { this.role = role; }

        public void setMaNV(String maNV) { this.maNV = maNV; }
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public UserData getUser() { return user; }

    public void setUser(UserData user) { this.user = user; }
}
