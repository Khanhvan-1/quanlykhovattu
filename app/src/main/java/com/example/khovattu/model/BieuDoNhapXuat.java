package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class BieuDoNhapXuat {
    @SerializedName("ngay")
    private String ngay;

    @SerializedName("tongNhap")
    private double tongNhap;

    @SerializedName("tongXuat")
    private double tongXuat;

    public String getNgay() { return ngay; }
    public double getTongNhap() { return tongNhap; }
    public double getTongXuat() { return tongXuat; }
}
