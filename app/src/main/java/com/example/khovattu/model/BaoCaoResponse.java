package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class BaoCaoResponse {
    @SerializedName("tongNhap")
    private int tongNhap;

    @SerializedName("tongXuat")
    private int tongXuat;

    @SerializedName("doanhThu")
    private double doanhThu;

    @SerializedName("loiNhuan")
    private double loiNhuan;

    @SerializedName("chiPhi")
    private double chiPhi;
    // Getters
    public int getTongNhap() { return tongNhap; }
    public int getTongXuat() { return tongXuat; }
    public double getDoanhThu() { return doanhThu; }
    public double getLoiNhuan() { return loiNhuan; }
    public double getChiPhi() { return chiPhi; }
}