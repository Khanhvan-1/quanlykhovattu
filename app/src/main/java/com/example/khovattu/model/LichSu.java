package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class LichSu {

    @SerializedName("_id")
    private String id;

    @SerializedName("loai")
    private String loai;

    @SerializedName("maPhieu")
    private String maPhieu;

    @SerializedName("tenSanPham")
    private String tenSanPham;

    @SerializedName("soLuong")
    private int soLuong;

    @SerializedName("gia")
    private double gia;

    @SerializedName("tongTien")
    private double tongTien;

    @SerializedName("userName")
    private String userName;

    @SerializedName("maNV")
    private String maNV;


    @SerializedName("userId")
    private String userId;

    @SerializedName("role")
    private String role;

    @SerializedName("nguoiThucHien")
    private String nguoiThucHien;

    @SerializedName("chucVu")
    private String chucVu;

    @SerializedName("createdAt")
    private String createdAt;

    public LichSu() {}


    public String getId() { return id; }
    public String getLoai() { return loai; }
    public String getMaPhieu() { return maPhieu; }
    public String getTenSanPham() { return tenSanPham; }
    public int getSoLuong() { return soLuong; }
    public double getGia() { return gia; }
    public double getTongTien() { return tongTien; }

    public String getUserName() {
        if (userName != null && !userName.isEmpty()) return userName;
        if (nguoiThucHien != null && !nguoiThucHien.isEmpty()) return nguoiThucHien;
        return "Hệ thống";
    }

    public String getMaNV() {
        if (maNV != null && !maNV.isEmpty()) return maNV;
        return "SYSTEM";
    }

    public String getUserId() { return userId; }

    public String getRole() {
        if (role != null && !role.isEmpty()) return role;
        if (chucVu != null && !chucVu.isEmpty()) return chucVu;
        return "system";
    }

    public String getCreatedAt() { return createdAt; }


    public void setId(String id) { this.id = id; }
    public void setLoai(String loai) { this.loai = loai; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public void setGia(double gia) { this.gia = gia; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public void setUserName(String userName) { this.userName = userName; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public void setUserId(String userId) { this.userId = userId; }

    public void setRole(String role) { this.role = role; }

    public void setNguoiThucHien(String nguoiThucHien) { this.nguoiThucHien = nguoiThucHien; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
