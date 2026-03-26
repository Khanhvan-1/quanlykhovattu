package com.example.khovattu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.adapter.LichSuListAdapter;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.LichSu;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LichSuActivity extends AppCompatActivity {

    private ListView lvLichSu;
    private LichSuListAdapter adapter;
    private List<LichSu> lichSuList = new ArrayList<>();
    private ApiService apiService;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;
    private String token, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lichsu);

        // 🔹 Ánh xạ view
        lvLichSu = findViewById(R.id.lvLichSu);
        bottomNav = findViewById(R.id.bottomNav);

        // 🔹 Khởi tạo API + session
        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        token = sessionManager.getToken();
        role = sessionManager.getRole();

        // 🔹 Adapter cho ListView
        adapter = new LichSuListAdapter(this, lichSuList);
        lvLichSu.setAdapter(adapter);

        // 🔹 Tải dữ liệu và setup menu
        loadLichSu();
        setupBottomNavigation();
    }

    /**
     * 🔹 Admin xem tất cả lịch sử, nhân viên chỉ xem lịch sử cá nhân
     */
    private void loadLichSu() {
        Call<List<LichSu>> call = role.equals("admin")
                ? apiService.layTatCaLichSu(sessionManager.getToken())
                : apiService.layLichSuCaNhan(sessionManager.getToken());

        call.enqueue(new Callback<List<LichSu>>() {
            @Override
            public void onResponse(Call<List<LichSu>> call, Response<List<LichSu>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lichSuList.clear();
                    lichSuList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(LichSuActivity.this, "⚠️ Không có dữ liệu lịch sử", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LichSu>> call, Throwable t) {
                Toast.makeText(LichSuActivity.this, "🚫 Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 🔻 Thanh điều hướng dưới cùng
     */
    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_lichsu);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)
                startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_sanpham)
                startActivity(new Intent(this, SanPhamActivity.class));
            else if (id == R.id.nav_khac)
                startActivity(new Intent(this, MenuKhacActivity.class));
            else if (id == R.id.nav_nhapkho)
                startActivity(new Intent(this, NhapKhoActivity.class));
            else if (id == R.id.nav_xuatkho)
                startActivity(new Intent(this, XuatKhoActivity.class));

            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }
}
