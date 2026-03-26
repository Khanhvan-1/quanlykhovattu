package com.example.khovattu.model;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String gioiTinh;

    public RegisterRequest(String username, String email, String password, String gioiTinh) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.gioiTinh = gioiTinh;
    }
}
