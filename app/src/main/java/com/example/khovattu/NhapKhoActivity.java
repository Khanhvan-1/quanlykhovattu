package com.example.khovattu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.PhieuResponse;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NhapKhoActivity extends AppCompatActivity {

    private LinearLayout btnSanPham, btnNhaCungCap, btnGhiChu;
    private Button btnLuu, btnThemNhanh;
    private BottomNavigationView bottomNav;
    private ImageButton btnBack;

    private ArrayList<SanPham> danhSachSanPhamDaChon = new ArrayList<>();
    private ActivityResultLauncher<Intent> chonSanPhamLauncher;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String token;

    // <<<========= THÊM BIẾN CHỌN NCC =========>>>
    private String nhaCungCap = "";
    private String ghiChu = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhap_kho);

        btnSanPham = findViewById(R.id.btnSanPham);
        btnNhaCungCap = findViewById(R.id.btnNhaCungCap);
        btnGhiChu = findViewById(R.id.btnGhiChu);
        btnLuu = findViewById(R.id.btnLuu);
        btnBack = findViewById(R.id.btnBack);
        bottomNav = findViewById(R.id.bottomNav);
        btnThemNhanh = findViewById(R.id.btnThemNhanh);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();

        btnBack.setOnClickListener(v -> finish());
        setupBottomNavByRole();

        // ------- CHỌN SẢN PHẨM -------
        chonSanPhamLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        danhSachSanPhamDaChon = result.getData()
                                .getParcelableArrayListExtra("DANH_SACH_SAN_PHAM_DA_CHON");

                        if (danhSachSanPhamDaChon != null && !danhSachSanPhamDaChon.isEmpty()) {
                            Toast.makeText(this,
                                    "Đã chọn " + danhSachSanPhamDaChon.size() + " sản phẩm",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    capNhatTrangThaiNutLuu();
                }
        );

        btnSanPham.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChonSanPhamActivity.class);
            chonSanPhamLauncher.launch(intent);
        });

        // ------- THÊM NHANH -------
        btnThemNhanh.setOnClickListener(v -> showDialogThemNhanhSanPham());

        // ------- CHỌN NCC DƯỚI DẠNG BOTTOMSHEET (MỚI) -------
        btnNhaCungCap.setOnClickListener(v -> {
            ChonNhaCungCapBottomSheet sheet =
                    new ChonNhaCungCapBottomSheet(ncc -> {

                        nhaCungCap = ncc.getTenNCC();   // ⭐ Lưu tên NCC
                        TextView txt = findViewById(R.id.txtNhaCungCap);

                        txt.setText(ncc.getTenNCC());   // ⭐ Hiển thị tên NCC

                        Toast.makeText(this,
                                "Chọn: " + ncc.getTenNCC(),
                                Toast.LENGTH_SHORT).show();
                    });

            sheet.show(getSupportFragmentManager(), "NCC");


        });



        // ------- GHI CHÚ -------
        btnGhiChu.setOnClickListener(v ->
                showTextInputDialog("Ghi chú", ghiChu, newText -> {
                    ghiChu = newText;
                    Toast.makeText(this, "Đã thêm ghi chú", Toast.LENGTH_SHORT).show();
                })
        );

        btnLuu.setOnClickListener(v -> nhapKhoVaLuuLichSu());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)
                startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_xuatkho)
                startActivity(new Intent(this, XuatKhoActivity.class));
            else if (id == R.id.nav_sanpham)
                startActivity(new Intent(this, SanPhamActivity.class));
            else if (id == R.id.nav_lichsu)
                startActivity(new Intent(this, LichSuActivity.class));
            else if (id == R.id.nav_khac)
                startActivity(new Intent(this, MenuKhacActivity.class));
            return true;
        });

        capNhatTrangThaiNutLuu();
    }

    // Interface CALLBACK chuẩn
    private interface OnTextEntered {
        void onEntered(String text);
    }

    private void showTextInputDialog(String title, String defaultText, OnTextEntered callback) {

        EditText input = new EditText(this);
        input.setText(defaultText);
        input.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    if (callback != null) callback.onEntered(input.getText().toString().trim());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    // ======= DIALOG THÊM NHANH =======
    private void showDialogThemNhanhSanPham() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_them_san_pham, null);

        EditText edtMa = view.findViewById(R.id.edtMaSPDialog);
        EditText edtTen = view.findViewById(R.id.edtTenSPDialog);
        EditText edtGia = view.findViewById(R.id.edtGiaNhapDialog);
        EditText edtSL = view.findViewById(R.id.edtSoLuongDialog);
        EditText edtGhiChu = view.findViewById(R.id.edtGhiChuDialog);
        Button btnTru = view.findViewById(R.id.btnTruSLNhap);
        Button btnCong = view.findViewById(R.id.btnCongSLNhap);
        Button btnHuy = view.findViewById(R.id.btnHuyNhapDialog);
        Button btnThem = view.findViewById(R.id.btnThemNhapDialog);

        edtSL.setText("1");

        btnTru.setOnClickListener(v -> {
            int sl = Math.max(1, parseIntSafe(edtSL.getText().toString()) - 1);
            edtSL.setText(String.valueOf(sl));
        });

        btnCong.setOnClickListener(v -> {
            int sl = parseIntSafe(edtSL.getText().toString()) + 1;
            edtSL.setText(String.valueOf(sl));
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        btnThem.setOnClickListener(v -> {
            String ma = edtMa.getText().toString().trim();
            String ten = edtTen.getText().toString().trim();
            String giaStr = edtGia.getText().toString().trim();
            String slStr = edtSL.getText().toString().trim();
            String note = edtGhiChu.getText().toString().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                Toast.makeText(this, "Mã và Tên sản phẩm bắt buộc!", Toast.LENGTH_SHORT).show();
                return;
            }

            double giaNhap = giaStr.isEmpty() ? 0 : Double.parseDouble(giaStr);
            int soLuong = slStr.isEmpty() ? 1 : Integer.parseInt(slStr);

            SanPham spMoi = new SanPham();
            spMoi.setMaHang(ma);
            spMoi.setTenHang(ten);
            spMoi.setGiaNhap(giaNhap);
            spMoi.setSoLuong(soLuong);
            spMoi.setGhiChu(note);

            danhSachSanPhamDaChon.add(spMoi);
            Toast.makeText(this, "Đã thêm: " + ten, Toast.LENGTH_SHORT).show();

            capNhatTrangThaiNutLuu();
            dialog.dismiss();
        });

        dialog.show();
    }

    private int parseIntSafe(String text) {
        try { return Integer.parseInt(text); }
        catch (Exception e) { return 1; }
    }


    // ======= LƯU PHIẾU NHẬP =======
    private void nhapKhoVaLuuLichSu() {

        if (danhSachSanPhamDaChon.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (SanPham sp : danhSachSanPhamDaChon) {
            if (sp.getSoLuong() <= 0) {
                Toast.makeText(this,
                        "Số lượng của " + sp.getTenHang() + " phải lớn hơn 0!",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("sanPham", danhSachSanPhamDaChon);

        if (!nhaCungCap.isEmpty()) body.put("nhaCungCap", nhaCungCap);
        if (!ghiChu.isEmpty()) body.put("ghiChu", ghiChu);

        Log.d("NhapKhoDebug", "Body gửi đi: " + body);

        apiService.nhapKho(token, body).enqueue(new Callback<PhieuResponse>() {
            @Override
            public void onResponse(Call<PhieuResponse> call, Response<PhieuResponse> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(NhapKhoActivity.this,
                            "Nhập kho thành công!",
                            Toast.LENGTH_SHORT).show();

                    // ⭐ Gửi tín hiệu cho SanPhamActivity kiểm tra tồn kho thấp
                    Intent intent = new Intent(NhapKhoActivity.this, SanPhamActivity.class);
                    intent.putExtra("CHECK_LOW_STOCK", true);

                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(NhapKhoActivity.this,
                            "Lỗi nhập kho! Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PhieuResponse> call, Throwable t) {
                Toast.makeText(NhapKhoActivity.this,
                        "Lỗi mạng: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void capNhatTrangThaiNutLuu() {
        btnLuu.setEnabled(danhSachSanPhamDaChon != null && !danhSachSanPhamDaChon.isEmpty());
    }

    private void setupBottomNavByRole() {
        String role = sessionManager.getRole();
        if (role == null) return;

        role = role.trim().toLowerCase();

        switch (role) {
            case "admin":
                for (int i = 0; i < bottomNav.getMenu().size(); i++)
                    bottomNav.getMenu().getItem(i).setVisible(true);
                break;

            case "nhap_kho":
                bottomNav.getMenu().findItem(R.id.nav_home).setVisible(false);
                bottomNav.getMenu().findItem(R.id.nav_xuatkho).setVisible(false);
                break;

            case "xuat_kho":
                bottomNav.getMenu().findItem(R.id.nav_home).setVisible(false);
                bottomNav.getMenu().findItem(R.id.nav_nhapkho).setVisible(false);
                break;

            default:
                for (int i = 0; i < bottomNav.getMenu().size(); i++)
                    bottomNav.getMenu().getItem(i).setVisible(false);
                bottomNav.getMenu().findItem(R.id.nav_khac).setVisible(true);
                break;
        }
    }
}
