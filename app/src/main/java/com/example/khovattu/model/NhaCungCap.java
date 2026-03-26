package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class NhaCungCap {

    @SerializedName("_id")
    private String id;

    private String tenNCC;
    private String soDienThoai;
    private String diaChi;

    public String getId() { return id; }
    public String getTenNCC() { return tenNCC; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getDiaChi() { return diaChi; }

    public void setId(String id) { this.id = id; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
}
