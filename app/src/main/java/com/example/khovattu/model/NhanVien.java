package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class NhanVien {

    @SerializedName("_id")
    private String id;

    @SerializedName("maNV")
    private String maNV;

    @SerializedName("tenNV")
    private String tenNV;

    @SerializedName("gioiTinh")
    private String gioiTinh;

    @SerializedName("tuoi")
    private int tuoi;

    @SerializedName("chucVu")
    private String chucVu;   // admin / nhap_kho / xuat_kho

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    // Constructor rỗng bắt buộc cho Gson
    public NhanVien() {}

    // Constructor đầy đủ (optional)
    public NhanVien(String maNV, String tenNV, String gioiTinh, int tuoi,
                    String chucVu, String email, String password) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.gioiTinh = gioiTinh;
        this.tuoi = tuoi;
        this.chucVu = chucVu;
        this.email = email;
        this.password = password;
    }

    // GETTER
    public String getId() { return id; }
    public String getMaNV() { return maNV; }
    public String getTenNV() { return tenNV; }
    public String getGioiTinh() { return gioiTinh; }
    public int getTuoi() { return tuoi; }
    public String getChucVu() { return chucVu; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Hỗ trợ adapter: trả về role
    public String getRole() { return chucVu; }

    // SETTER
    public void setId(String id) { this.id = id; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public void setTuoi(int tuoi) { this.tuoi = tuoi; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
