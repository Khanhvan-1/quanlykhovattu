package com.example.khovattu.model;

import com.google.gson.annotations.SerializedName;

public class NhanVienResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("nv")
    private NhanVien nhanVien;

    // Nếu API bạn có success thì thêm:
    @SerializedName("success")
    private boolean success;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public NhanVien getNhanVien() { return nhanVien; }
}
