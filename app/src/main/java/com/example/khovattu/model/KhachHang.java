package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class KhachHang {

    @SerializedName("_id")
    private String id;

    @SerializedName("ten")
    private String ten;

    @SerializedName("soDienThoai")
    private String soDienThoai;

    @SerializedName("diaChi")
    private String diaChi;

    @SerializedName("email")
    private String email;

    // --- Getter / Setter ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
