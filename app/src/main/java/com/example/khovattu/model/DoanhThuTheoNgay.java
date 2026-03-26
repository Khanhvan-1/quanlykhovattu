package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class DoanhThuTheoNgay {
    @SerializedName("ngay")
    private String ngay;

    @SerializedName("doanhThu")
    private double doanhThu;

    public String getNgay() { return ngay; }
    public double getDoanhThu() { return doanhThu; }
}