package com.example.khovattu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.KhachHang;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KhachHangActivity extends AppCompatActivity {

    private ListView listKhachHang;
    private FloatingActionButton btnThemKhachHang;
    private ArrayList<KhachHang> dsKhachHang = new ArrayList<>();
    private ArrayList<String> dsTen = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String token;
    private static final int REQ_THEM_KHACH = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_khachhang);

        listKhachHang = findViewById(R.id.listKhachHang);
        btnThemKhachHang = findViewById(R.id.btnThemKhachHang);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dsTen);
        listKhachHang.setAdapter(adapter);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();


        taiDanhSachKhachHang();

        listKhachHang.setOnItemClickListener((parent, view, position, id) -> {
            KhachHang kh = dsKhachHang.get(position);
            Intent result = new Intent();
            result.putExtra("KHACH_HANG_ID", kh.getId());
            result.putExtra("KHACH_HANG_TEN", kh.getTen());
            setResult(Activity.RESULT_OK, result);
            finish();
        });

        btnThemKhachHang.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemKhachHangActivity.class);
            startActivityForResult(intent, REQ_THEM_KHACH);
        });
    }

    private void taiDanhSachKhachHang() {
        apiService.layDanhSachKhachHang(token).enqueue(new Callback<List<KhachHang>>() {
            @Override
            public void onResponse(Call<List<KhachHang>> call, Response<List<KhachHang>> response) {
                dsKhachHang.clear();
                dsTen.clear();
                if (response.isSuccessful() && response.body() != null) {
                    dsKhachHang.addAll(response.body());
                    for (KhachHang kh : dsKhachHang) {
                        dsTen.add("👤 " + kh.getTen() + " • " + kh.getSoDienThoai());
                    }
                } else {
                    dsTen.add("Không có dữ liệu khách hàng!");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<KhachHang>> call, Throwable t) {
                dsTen.clear();
                dsTen.add("❌ Lỗi tải dữ liệu: " + t.getMessage());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_THEM_KHACH && resultCode == Activity.RESULT_OK) {
            taiDanhSachKhachHang();
            Toast.makeText(this, "✅ Đã thêm khách hàng mới!", Toast.LENGTH_SHORT).show();
        }
    }
}
