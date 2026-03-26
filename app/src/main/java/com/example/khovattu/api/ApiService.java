package com.example.khovattu.api;

import com.example.khovattu.model.*;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @FormUrlEncoded
    @POST("auth/change-password")
    Call<Map<String, Object>> doiMatKhau(
            @Header("Authorization") String token,
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword
    );

    @GET("auth/users")
    Call<List<UserModel>> getAllUsers(@Header("Authorization") String token);

    @DELETE("auth/users/{id}")
    Call<Void> xoaUser(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @PUT("auth/users/{id}")
    Call<Map<String, Object>> capNhatUser(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body Map<String, Object> body
    );

    @POST("auth/users")
    Call<Map<String, Object>> themUser(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );


    // ===============================
    // 📌 EXPORT EXCEL
    // ===============================
    @GET("api/export/excel")
    Call<ResponseBody> exportExcel(@Header("Authorization") String token);

    @GET("api/export/tonkho")
    Call<ResponseBody> exportTonKho(@Header("Authorization") String token);

    @GET("api/export/theongay")
    Call<ResponseBody> exportTheoNgay(
            @Header("Authorization") String token,
            @Query("from") String from,
            @Query("to") String to
    );


    // ===============================
    // 📌 NHÂN VIÊN
    // ===============================
    @GET("api/nhanvien")
    Call<List<NhanVien>> layDanhSachNhanVien(@Header("Authorization") String token);

    @POST("api/nhanvien")
    Call<NhanVienResponse> themNhanVien(
            @Header("Authorization") String token,
            @Body NhanVien nhanVien
    );

    @DELETE("api/nhanvien/{maNV}")
    Call<Void> xoaNhanVien(
            @Header("Authorization") String token,
            @Path("maNV") String maNV
    );

    // ⭐ CẬP NHẬT NHÂN VIÊN (bạn chưa có)
    @PUT("api/nhanvien/{maNV}")
    Call<NhanVienResponse> capNhatNhanVien(
            @Header("Authorization") String token,
            @Path("maNV") String maNV,
            @Body NhanVien nhanVien
    );

    // ⭐ LỌC NHÂN VIÊN (phải đúng đường dẫn backend)
    @GET("api/nhanvien/loc/{role}")
    Call<List<NhanVien>> locNhanVienTheoRole(
            @Header("Authorization") String token,
            @Path("role") String role
    );


    // ===============================
    // 📌 KHÁCH HÀNG
    // ===============================
    @GET("api/khachhang")
    Call<List<KhachHang>> layDanhSachKhachHang(@Header("Authorization") String token);

    @POST("api/khachhang")
    Call<KhachHang> themKhachHang(
            @Header("Authorization") String token,
            @Body KhachHang kh
    );


    // ===============================
    // 📌 NHÀ CUNG CẤP
    // ===============================
    @GET("api/ncc")
    Call<List<NhaCungCap>> layDanhSachNCC(@Header("Authorization") String token);

    @POST("api/ncc")
    Call<NhaCungCap> themNCC(
            @Header("Authorization") String token,
            @Body NhaCungCap ncc
    );

    @PUT("api/ncc/{id}")
    Call<NhaCungCap> capNhatNCC(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body NhaCungCap ncc
    );

    @DELETE("api/ncc/{id}")
    Call<Void> xoaNCC(
            @Header("Authorization") String token,
            @Path("id") String id
    );


    // ===============================
    // 📌 SẢN PHẨM
    // ===============================
    @GET("api/sanpham")
    Call<List<SanPham>> layDanhSachSanPham(@Header("Authorization") String token);

    @GET("api/sanpham/{id}")
    Call<SanPham> layChiTietSanPham(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @POST("api/sanpham")
    Call<SanPham> themSanPham(
            @Header("Authorization") String token,
            @Body SanPham sanPham
    );

    @PUT("api/sanpham/{id}")
    Call<SanPham> capNhatSanPham(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body SanPham sanPham
    );

    @DELETE("api/sanpham/{id}")
    Call<Void> xoaSanPham(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @GET("api/sanpham/search")
    Call<List<SanPham>> timKiemSanPham(
            @Header("Authorization") String token,
            @Query("keyword") String keyword
    );

    @POST("api/sanpham/phanloai/{id}")
    Call<Map<String, Object>> phanLoaiSanPham(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @FormUrlEncoded
    @POST("api/sanpham/canhbao")
    Call<Map<String, Object>> guiCanhBaoTonKho(
            @Header("Authorization") String token,
            @Field("maHang") String maHang,
            @Field("soLuong") int soLuong
    );


    // ===============================
    // 📌 PHIẾU NHẬP – PHIẾU XUẤT
    // ===============================
    @POST("api/nhapkho")
    Call<PhieuResponse> nhapKho(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );

    @POST("api/xuatkho")
    Call<PhieuResponse> xuatKho(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );


    // ===============================
    // 📌 LỊCH SỬ
    // ===============================
    @GET("api/lichsu")
    Call<List<LichSu>> layTatCaLichSu(@Header("Authorization") String token);

    @GET("api/lichsu/ca-nhan")
    Call<List<LichSu>> layLichSuCaNhan(@Header("Authorization") String token);

    @GET("api/lichsu/loc-ngay")
    Call<List<LichSu>> locLichSuTheoNgay(
            @Header("Authorization") String token,
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("api/lichsu/loai")
    Call<List<LichSu>> locLichSuTheoLoai(
            @Header("Authorization") String token,
            @Query("type") String type
    );


    // ===============================
    // 📌 BÁO CÁO
    // ===============================
    @GET("api/baocao")
    Call<BaoCaoResponse> layBaoCaoTongHop(@Header("Authorization") String token);

    @GET("api/baocao/{type}")
    Call<Map<String, Object>> layBaoCaoTheoLoai(
            @Header("Authorization") String token,
            @Path("type") String type
    );

    @GET("api/baocao/tonkho")
    Call<List<BaoCaoTonKho>> layBaoCaoTonKho(@Header("Authorization") String token);

    @GET("api/baocao/bieudo/nhapxuat")
    Call<List<BieuDoNhapXuat>> layBieuDoNhapXuat(
            @Header("Authorization") String token,
            @Query("from") String from,
            @Query("to") String to
    );


    // ===============================
    // 📌 SMS CẢNH BÁO
    // ===============================
    @FormUrlEncoded
    @POST("api/sms/canhbao")
    Call<Map<String, Object>> guiSmsCanhBao(
            @Header("Authorization") String token,
            @Field("soDienThoai") String soDienThoai,
            @Field("noiDung") String noiDung
    );
}
