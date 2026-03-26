package com.example.khovattu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuKhacActivity extends AppCompatActivity {

    private TextView tvTenNguoiDung, tvEmailNguoiDung, tvRoleNguoiDung;
    private LinearLayout btnQLNguoiDung, btnCaiDat, btnDangXuat;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_khac);

        tvTenNguoiDung = findViewById(R.id.tvTenNguoiDung);
        tvEmailNguoiDung = findViewById(R.id.tvEmailNguoiDung);
        tvRoleNguoiDung = findViewById(R.id.tvRoleNguoiDung);
        btnQLNguoiDung = findViewById(R.id.btnQLNguoiDung);
        btnCaiDat = findViewById(R.id.btnCaiDat);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        bottomNav = findViewById(R.id.bottomNav);

        sessionManager = new SessionManager(this);

        // 🧑‍💻 Hiển thị thông tin người dùng
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        String role = sessionManager.getRole();

        tvTenNguoiDung.setText(username != null && !username.isEmpty() ? username : "Người dùng");
        tvEmailNguoiDung.setText(email != null && !email.isEmpty() ? email : "user@gmail.com");
        tvRoleNguoiDung.setText("Quyền: " + (role != null ? role : "User"));

        // 🔹 Phân quyền hiển thị menu
        if ("admin".equalsIgnoreCase(role)) {
            btnQLNguoiDung.setVisibility(LinearLayout.VISIBLE);
            btnCaiDat.setVisibility(LinearLayout.VISIBLE);
        } else {
            btnQLNguoiDung.setVisibility(LinearLayout.GONE);
            btnCaiDat.setVisibility(LinearLayout.VISIBLE);
        }

        // 🔹 Sự kiện click
        btnQLNguoiDung.setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyUserActivity.class))
        );

        btnCaiDat.setOnClickListener(v ->
                startActivity(new Intent(this, CaiDatActivity.class))
        );

        btnDangXuat.setOnClickListener(v -> {
            Toast.makeText(this, "🚪 Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            sessionManager.clearSession();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        // 🔹 Thanh điều hướng
        setupBottomNav(role);
    }

    private void setupBottomNav(String role) {
        bottomNav.setSelectedItemId(R.id.nav_khac);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MenuKhacActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_sanpham) {
                startActivity(new Intent(MenuKhacActivity.this, SanPhamActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_lichsu) {
                startActivity(new Intent(MenuKhacActivity.this, LichSuActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_khac) {
                return true;
            } else {
                Toast.makeText(MenuKhacActivity.this, "🔒 Không thể mở trang này.", Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }
}
