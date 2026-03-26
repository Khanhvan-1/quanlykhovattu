package com.example.khovattu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.adapter.ChonSanPhamAdapter;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChonSanPhamActivity extends AppCompatActivity {

    private RecyclerView rvSanPham;
    private Button btnHoanThanh;
    private ImageButton btnBack, btnThem;
    private TextView tvTongSoLuong, tvTongGia;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String token;

    private ChonSanPhamAdapter adapter;
    private List<SanPham> sanPhamList = new ArrayList<>();
    private ActivityResultLauncher<Intent> themSanPhamLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_san_pham);

        rvSanPham = findViewById(R.id.rvSanPham);
        btnHoanThanh = findViewById(R.id.btnHoanThanh);
        btnBack = findViewById(R.id.btnBack);
        btnThem = findViewById(R.id.btnThem);
        tvTongSoLuong = findViewById(R.id.tvTongSoLuong);
        tvTongGia = findViewById(R.id.tvTongGia);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();

        themSanPhamLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) loadSanPhams();
                }
        );

        setupRecyclerView();
        loadSanPhams();

        btnBack.setOnClickListener(v -> finish());
        btnThem.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemSanPhamActivity.class);
            themSanPhamLauncher.launch(intent);
        });

        btnHoanThanh.setOnClickListener(v -> {
            ArrayList<SanPham> danhSachDaChon = adapter.getDanhSachDaChon();
            if (danhSachDaChon.isEmpty()) {
                Toast.makeText(this, "Bạn chưa chọn sản phẩm nào", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🚫 CHẶN SỐ ÂM / 0
            for (SanPham sp : danhSachDaChon) {
                if (sp.getSoLuong() <= 0) {
                    Toast.makeText(this,
                            "Số lượng của " + sp.getTenHang() + " phải lớn hơn 0!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("DANH_SACH_SAN_PHAM_DA_CHON", danhSachDaChon);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void setupRecyclerView() {
        adapter = new ChonSanPhamAdapter(sanPhamList, this);
        rvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rvSanPham.setAdapter(adapter);

        adapter.setOnSelectionChangedListener((tongSP, tongGia) -> {
            DecimalFormat df = new DecimalFormat("###,###,###");
            tvTongSoLuong.setText(tongSP + " sản phẩm");
            tvTongGia.setText("Tổng giá: " + df.format(tongGia) + " ₫");
        });
    }

    private void loadSanPhams() {
        apiService.layDanhSachSanPham(token).enqueue(new Callback<List<SanPham>>() {
            @Override
            public void onResponse(Call<List<SanPham>> call, Response<List<SanPham>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sanPhamList.clear();
                    sanPhamList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ChonSanPhamActivity.this, "Không thể tải danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SanPham>> call, Throwable t) {
                Toast.makeText(ChonSanPhamActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
