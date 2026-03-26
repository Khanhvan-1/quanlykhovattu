package com.example.khovattu.model;

public class PhieuResponse {

    private String message;
    private String maPhieu;
    private SanPham sanPham;
    private int soLuong;
    private String loai;     // "Nhập" hoặc "Xuất"
    private String createdAt;

    // --- Getter ---
    public String getMessage() {
        return message;
    }

    public String getMaPhieu() {
        return maPhieu;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public String getLoai() {
        return loai;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // --- Setter ---
    public void setMessage(String message) {
        this.message = message;
    }

    public void setMaPhieu(String maPhieu) {
        this.maPhieu = maPhieu;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
