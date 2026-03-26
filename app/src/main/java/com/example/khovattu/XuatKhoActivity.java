package com.example.khovattu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.KhachHang;
import com.example.khovattu.model.PhieuResponse;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XuatKhoActivity extends AppCompatActivity {

    private RelativeLayout rowSanPham, rowKhachHang, rowGhiChu;
    private TextView txtTenKhachHang;
    private Button btnLuu;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;

    private ArrayList<SanPham> danhSachSanPhamDaChon = new ArrayList<>();
    private ActivityResultLauncher<Intent> chonSanPhamLauncher;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String token;

    private KhachHang khachHangDaChon = null;
    private String ghiChu = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xuat_kho);

        // ======== Ánh xạ ========
        rowSanPham = findViewById(R.id.rowSanPham);
        rowKhachHang = findViewById(R.id.rowKhachHang);
        rowGhiChu = findViewById(R.id.rowGhiChu);
        txtTenKhachHang = findViewById(R.id.txtTenKhachHang);
        btnLuu = findViewById(R.id.btnLuu);
        bottomNav = findViewById(R.id.bottomNav);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();

        setupBottomNavByRole();

        // ======== Chọn sản phẩm ========
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
                    capNhatNutLuu();
                }
        );

        rowSanPham.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChonSanPhamActivity.class);
            chonSanPhamLauncher.launch(intent);
        });

        // ======== CHỌN KHÁCH HÀNG (BottomSheet) ========
        rowKhachHang.setOnClickListener(v -> {

            ChonKhachHangBottomSheet sheet = new ChonKhachHangBottomSheet(kh -> {
                khachHangDaChon = kh;
                txtTenKhachHang.setText(kh.getTen()); // HIỂN THỊ TÊN CHÍNH XÁC

                Toast.makeText(this,
                        "Chọn: " + kh.getTen(),
                        Toast.LENGTH_SHORT).show();
            });

            sheet.show(getSupportFragmentManager(), "KH");
        });


        // ======== Nhập ghi chú ========
        rowGhiChu.setOnClickListener(v ->
                showTextInputDialog("Ghi chú", ghiChu, text -> {
                    ghiChu = text;
                    Toast.makeText(this, "Đã thêm ghi chú", Toast.LENGTH_SHORT).show();
                })
        );

        btnLuu.setOnClickListener(v -> xuatKhoVaLuuLichSu());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)
                startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_nhapkho)
                startActivity(new Intent(this, NhapKhoActivity.class));
            else if (id == R.id.nav_sanpham)
                startActivity(new Intent(this, SanPhamActivity.class));
            else if (id == R.id.nav_lichsu)
                startActivity(new Intent(this, LichSuActivity.class));
            else if (id == R.id.nav_khac)
                startActivity(new Intent(this, MenuKhacActivity.class));

            return true;
        });

        capNhatNutLuu();
    }

    // ======== Hộp nhập text ========
    private void showTextInputDialog(String title, String defaultText, OnTextEnteredListener listener) {
        EditText input = new EditText(this);
        input.setText(defaultText);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) ->
                        listener.onTextEntered(input.getText().toString().trim()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private interface OnTextEnteredListener {
        void onTextEntered(String text);
    }

    private void capNhatNutLuu() {
        btnLuu.setEnabled(danhSachSanPhamDaChon != null && !danhSachSanPhamDaChon.isEmpty());
    }

    // ======== Gửi API Xuất Kho ========
    private void xuatKhoVaLuuLichSu() {

        if (danhSachSanPhamDaChon == null || danhSachSanPhamDaChon.isEmpty()) {
            Toast.makeText(this, "⚠️ Vui lòng chọn sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> body = new HashMap<>();
        body.put("sanPham", danhSachSanPhamDaChon);

        if (khachHangDaChon != null)
            body.put("khachHang", khachHangDaChon.getTen());
        if (!ghiChu.isEmpty())
            body.put("ghiChu", ghiChu);

        Log.d("XuatKhoDebug", "Body gửi đi: " + body);

        apiService.xuatKho(token, body).enqueue(new Callback<PhieuResponse>() {
            @Override
            public void onResponse(Call<PhieuResponse> call, Response<PhieuResponse> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(XuatKhoActivity.this,
                            "Xuất kho thành công!",
                            Toast.LENGTH_SHORT).show();

                    // ⭐ Gửi tín hiệu kiểm tra tồn kho thấp
                    Intent intent = new Intent(XuatKhoActivity.this, SanPhamActivity.class);
                    intent.putExtra("CHECK_LOW_STOCK", true);

                    startActivity(intent);
                    finish();
                }
                else {
                    String errorMsg = "Lỗi hệ thống.";

                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            JSONObject obj = new JSONObject(err);
                            errorMsg = obj.optString("message", err);
                        }
                    } catch (Exception ignored) {}

                    new AlertDialog.Builder(XuatKhoActivity.this)
                            .setTitle("Xuất kho thất bại")
                            .setMessage(errorMsg)
                            .setPositiveButton("Đóng", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<PhieuResponse> call, Throwable t) {
                new AlertDialog.Builder(XuatKhoActivity.this)
                        .setTitle("Mất kết nối")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    // ======== Quyền menu theo role ========
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
