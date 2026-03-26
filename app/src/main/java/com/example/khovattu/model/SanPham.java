package com.example.khovattu.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class SanPham implements Parcelable {

    @SerializedName("_id")
    private String id;

    @SerializedName("maHang")
    private String maHang;

    @SerializedName("tenHang")
    private String tenHang;

    @SerializedName("loaiHang")
    private String loaiHang;

    @SerializedName("soLuong")
    private int soLuong;

    @SerializedName("tonKho")
    private int tonKho;

    @SerializedName("giaNhap")
    private double giaNhap;

    @SerializedName("giaXuat")
    private double giaXuat;

    @SerializedName("ghiChu")
    private String ghiChu;

    @SerializedName("khachHang")
    private String khachHang;

    @SerializedName("tongNhap")
    private int tongNhap;

    @SerializedName("tongXuat")
    private int tongXuat;

    // --- Constructor ---
    public SanPham() {
        this.loaiHang = "Khác";   // 🔥 tránh null khi backend chưa trả về
    }

    protected SanPham(Parcel in) {
        id = in.readString();
        maHang = in.readString();
        tenHang = in.readString();

        // 🔥 FIX: tránh null khi đọc Parcel
        String lh = in.readString();
        loaiHang = (lh == null || lh.isEmpty()) ? "Khác" : lh;

        soLuong = in.readInt();
        tonKho = in.readInt();
        giaNhap = in.readDouble();
        giaXuat = in.readDouble();
        ghiChu = in.readString();
        khachHang = in.readString();
        tongNhap = in.readInt();
        tongXuat = in.readInt();
    }

    public static final Creator<SanPham> CREATOR = new Creator<SanPham>() {
        @Override
        public SanPham createFromParcel(Parcel in) {
            return new SanPham(in);
        }

        @Override
        public SanPham[] newArray(int size) {
            return new SanPham[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(maHang);
        dest.writeString(tenHang);

        // 🔥 luôn ghi loại hợp lệ
        dest.writeString(loaiHang != null ? loaiHang : "Khác");

        dest.writeInt(soLuong);
        dest.writeInt(tonKho);
        dest.writeDouble(giaNhap);
        dest.writeDouble(giaXuat);
        dest.writeString(ghiChu);
        dest.writeString(khachHang);
        dest.writeInt(tongNhap);
        dest.writeInt(tongXuat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // --- Getter / Setter ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getLoaiHang() {
        return (loaiHang == null || loaiHang.equals("null")) ? "Khác" : loaiHang;
    }

    public void setLoaiHang(String loaiHang) {
        // 🔥 tránh null phá UI
        this.loaiHang = (loaiHang == null || loaiHang.equals("null")) ? "Khác" : loaiHang;
    }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }

    public double getGiaXuat() { return giaXuat; }
    public void setGiaXuat(double giaXuat) { this.giaXuat = giaXuat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }

    public int getTongNhap() { return tongNhap; }
    public void setTongNhap(int tongNhap) { this.tongNhap = tongNhap; }

    public int getTongXuat() { return tongXuat; }
    public void setTongXuat(int tongXuat) { this.tongXuat = tongXuat; }
}
