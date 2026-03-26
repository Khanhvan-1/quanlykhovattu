package com.example.khovattu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.BaoCaoTonKho;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TonKhoActivity extends AppCompatActivity {

    private ArrayList<HashMap<String, String>> tonKhoList;
    private ListView lvTonKho;
    private ApiService apiService;
    private SimpleAdapter adapter;
    private SessionManager sessionManager;
    private String token;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao_tonkho);

        lvTonKho = findViewById(R.id.lvTonKho);
        bottomNav = findViewById(R.id.bottomNav);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        tonKhoList = new ArrayList<>();
        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();


        adapter = new SimpleAdapter(
                this,
                tonKhoList,
                R.layout.item_ton_kho,
                new String[]{"tenHang", "maHang", "nhap", "xuat", "tonKho"},
                new int[]{R.id.tvTenSP, R.id.tvMaSP, R.id.tvNhap, R.id.tvXuat, R.id.tvTonKho}
        );
        lvTonKho.setAdapter(adapter);

        setupBottomNavByRole();
        setupBottomNavEvents();

        loadTonKho();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTonKho();
    }

    // ================================
    // 🚀 LOAD API TỒN KHO
    // ================================
    private void loadTonKho() {
        apiService.layBaoCaoTonKho(token).enqueue(new Callback<List<BaoCaoTonKho>>() {
            @Override
            public void onResponse(Call<List<BaoCaoTonKho>> call, Response<List<BaoCaoTonKho>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(TonKhoActivity.this, "❌ Không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

                tonKhoList.clear();

                for (BaoCaoTonKho bc : response.body()) {

                    HashMap<String, String> map = new HashMap<>();
                    map.put("tenHang", "Tên hàng: " + bc.getTenHang());
                    map.put("maHang", "Mã hàng: " + bc.getMaHang());
                    map.put("nhap", "Nhập: +" + bc.getTongNhap());
                    map.put("xuat", "Xuất: -" + bc.getTongXuat());
                    map.put("tonKho", "Tồn kho: " + bc.getTonKho() + " sp");

                    tonKhoList.add(map);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<BaoCaoTonKho>> call, Throwable t) {
                Toast.makeText(TonKhoActivity.this, "🚫 Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ================================
    // 🔘 MENU QUYỀN
    // ================================
    private void setupBottomNavByRole() {
        if (bottomNav == null) return;

        String role = sessionManager.getRole();
        if (role == null) return;
        role = role.toLowerCase();

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
                break;
        }
    }

    private void setupBottomNavEvents() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home)
                startActivity(new Intent(this, MainActivity.class));
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
            finish();
            return true;
        });
    }
}
