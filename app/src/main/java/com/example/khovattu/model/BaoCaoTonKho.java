package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class BaoCaoTonKho {

    @SerializedName("maHang")
    private String maHang;

    @SerializedName("tenHang")
    private String tenHang;

    @SerializedName("nhap")
    private int tongNhap;

    @SerializedName("xuat")
    private int tongXuat;

    @SerializedName("tonKho")
    private int tonKho;

    public String getMaHang() {
        return maHang;
    }

    public String getTenHang() {
        return tenHang;
    }

    public int getTongNhap() {
        return tongNhap;
    }

    public int getTongXuat() {
        return tongXuat;
    }

    public int getTonKho() {
        return tonKho;
    }
}
