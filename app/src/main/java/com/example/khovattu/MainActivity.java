package com.example.khovattu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.BieuDoNhapXuat;
import com.example.khovattu.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ApiService apiService;
    private LineChart chartDoanhThuTuan;
    private BottomNavigationView bottomNav;

    private LinearLayout layoutNhapKho, layoutXuatKho, layoutNhanVien, layoutKhachHang,
            layoutBaoCao, layoutNhaCungCap;

    private static final int REQUEST_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        apiService = ApiClient.getClient(this).create(ApiService.class);

        // 🔐 Kiểm tra đăng nhập
        if (!sessionManager.isLoggedIn() || !sessionManager.isTokenValid()) {
            Toast.makeText(this, "⚠️ Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Ánh xạ view
        chartDoanhThuTuan = findViewById(R.id.chartDoanhThuTuan);
        bottomNav = findViewById(R.id.bottomNav);

        layoutNhapKho = findViewById(R.id.layoutNhapKho);
        layoutXuatKho = findViewById(R.id.layoutXuatKho);
        layoutNhanVien = findViewById(R.id.layoutNhanVien);
        layoutKhachHang = findViewById(R.id.layoutKhachHang);
        layoutBaoCao   = findViewById(R.id.layoutBaoCao);

        // ⭐ THÊM MỚI - Nhà cung cấp
        layoutNhaCungCap = findViewById(R.id.layoutNhaCungCap);

        setupBottomNavigation();
        setupBottomNavByRole();
        requestNecessaryPermissions();
        setupButtonEvents();
        loadNhapXuatChart();
    }

    private void loadNhapXuatChart() {
        String token = sessionManager.getToken();

        apiService.layBieuDoNhapXuat(token, null, null).enqueue(new Callback<List<BieuDoNhapXuat>>() {
            @Override
            public void onResponse(Call<List<BieuDoNhapXuat>> call, Response<List<BieuDoNhapXuat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupChartNhapXuat(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "⚠ Không có dữ liệu biểu đồ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BieuDoNhapXuat>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "❌ Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChartNhapXuat(List<BieuDoNhapXuat> list) {

        ArrayList<Entry> eNhap = new ArrayList<>();
        ArrayList<Entry> eXuat = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        float maxY = 0f;

        SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM");

        for (int i = 0; i < list.size(); i++) {
            BieuDoNhapXuat bx = list.get(i);

            float nhap = (float) bx.getTongNhap();
            float xuat = (float) bx.getTongXuat();

            eNhap.add(new Entry(i, nhap));
            eXuat.add(new Entry(i, xuat));

            try {
                Date d = sdfIn.parse(bx.getNgay());
                labels.add(sdfOut.format(d));
            } catch (Exception e) {
                labels.add(bx.getNgay());
            }

            if (nhap > maxY) maxY = nhap;
            if (xuat > maxY) maxY = xuat;
        }

        float upper = maxY * 1.15f;

        LineDataSet setNhap = new LineDataSet(eNhap, "Nhập");
        setNhap.setColor(Color.parseColor("#2B7CFF"));
        setNhap.setCircleColor(Color.parseColor("#2B7CFF"));
        setNhap.setCircleRadius(4f);

        LineDataSet setXuat = new LineDataSet(eXuat, "Xuất");
        setXuat.setColor(Color.parseColor("#FF6B6B"));
        setXuat.setCircleColor(Color.parseColor("#FF6B6B"));
        setXuat.setCircleRadius(4f);

        LineData data = new LineData(setNhap, setXuat);
        chartDoanhThuTuan.setData(data);

        XAxis xAxis = chartDoanhThuTuan.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        chartDoanhThuTuan.getAxisLeft().setAxisMinimum(0f);
        chartDoanhThuTuan.getAxisLeft().setAxisMaximum(upper);
        chartDoanhThuTuan.getAxisRight().setEnabled(false);

        chartDoanhThuTuan.getDescription().setEnabled(false);

        chartDoanhThuTuan.animateY(1200);
        chartDoanhThuTuan.invalidate();
    }

    private void setupButtonEvents() {
        String role = sessionManager.getRole().trim().toLowerCase();

        layoutNhapKho.setOnClickListener(v -> {
            if ("xuat_kho".equals(role))
                Toast.makeText(this, "🚫 Nhân viên xuất kho không được nhập hàng!", Toast.LENGTH_SHORT).show();
            else
                startActivity(new Intent(this, NhapKhoActivity.class));
        });

        layoutXuatKho.setOnClickListener(v -> {
            if ("nhap_kho".equals(role))
                Toast.makeText(this, "🚫 Nhân viên nhập kho không được xuất hàng!", Toast.LENGTH_SHORT).show();
            else
                startActivity(new Intent(this, XuatKhoActivity.class));
        });

        layoutNhanVien.setOnClickListener(v -> {
            if ("admin".equals(role))
                startActivity(new Intent(this, QuanLyNhanVienActivity.class));
            else
                Toast.makeText(this, "⚠️ Chỉ admin được truy cập!", Toast.LENGTH_SHORT).show();
        });

        layoutKhachHang.setOnClickListener(v ->
                startActivity(new Intent(this, KhachHangActivity.class))
        );

        layoutBaoCao.setOnClickListener(v -> hienDialogBaoCao());

        // ⭐ THÊM MỚI – BUTTON NHÀ CUNG CẤP
        layoutNhaCungCap.setOnClickListener(v ->
                startActivity(new Intent(this, NhaCungCapActivity.class))
        );
    }

    private void hienDialogBaoCao() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_baocao, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        view.findViewById(R.id.btnBaoCaoTaiChinh).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, BaoCaoTaiChinhActivity.class));
        });

        view.findViewById(R.id.btnBaoCaoTonKho).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, TonKhoActivity.class));
        });

        dialog.show();
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_nhapkho)
                startActivity(new Intent(this, NhapKhoActivity.class));
            else if (id == R.id.nav_xuatkho)
                startActivity(new Intent(this, XuatKhoActivity.class));
            else if (id == R.id.nav_sanpham)
                startActivity(new Intent(this, SanPhamActivity.class));
            else if (id == R.id.nav_lichsu)
                startActivity(new Intent(this, LichSuActivity.class));
            else if (id == R.id.nav_khac)
                startActivity(new Intent(this, MenuKhacActivity.class));

            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void setupBottomNavByRole() {
        String role = sessionManager.getRole();
        if (role == null) return;

        role = role.trim().toLowerCase();

        switch (role) {
            case "admin":
                break;

            case "nhap_kho":
                bottomNav.getMenu().findItem(R.id.nav_xuatkho).setVisible(false);
                break;

            case "xuat_kho":
                bottomNav.getMenu().findItem(R.id.nav_nhapkho).setVisible(false);
                break;

            default:
                for (int i = 0; i < bottomNav.getMenu().size(); i++)
                    bottomNav.getMenu().getItem(i).setVisible(false);
                bottomNav.getMenu().findItem(R.id.nav_khac).setVisible(true);
        }
    }

    private void requestNecessaryPermissions() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS, Manifest.permission.POST_NOTIFICATIONS}
                : new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS};

        boolean needRequest = false;

        for (String p : permissions)
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                needRequest = true;

        if (needRequest)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;

            for (int res : grantResults)
                if (res != PackageManager.PERMISSION_GRANTED) allGranted = false;

            Toast.makeText(this,
                    allGranted ? "✅ Đã cấp đủ quyền!"
                            : "⚠️ Một số quyền bị từ chối.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
