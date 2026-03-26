package com.example.khovattu;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.KhachHang;
import com.example.khovattu.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemKhachHangActivity extends AppCompatActivity {

    private EditText edtTenKH, edtSdtKH, edtDiaChiKH, edtEmailKH;
    private Button btnLuuKH;
    private ApiService apiService;
    private SessionManager sessionManager;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_khach_hang);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtTenKH = findViewById(R.id.edtTenKH);
        edtSdtKH = findViewById(R.id.edtSdtKH);
        edtDiaChiKH = findViewById(R.id.edtDiaChiKH);
        edtEmailKH = findViewById(R.id.edtEmailKH);
        btnLuuKH = findViewById(R.id.btnLuuKH);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();


        btnLuuKH.setOnClickListener(v -> handleThemKhachHang());
    }

    private void handleThemKhachHang() {
        String ten = edtTenKH.getText().toString().trim();
        String sdt = edtSdtKH.getText().toString().trim();
        String diaChi = edtDiaChiKH.getText().toString().trim();
        String email = edtEmailKH.getText().toString().trim();

        if (ten.isEmpty() || sdt.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        KhachHang kh = new KhachHang();
        kh.setTen(ten);
        kh.setSoDienThoai(sdt);
        kh.setDiaChi(diaChi);
        kh.setEmail(email);

        apiService.themKhachHang(token, kh).enqueue(new Callback<KhachHang>() {
            @Override
            public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ThemKhachHangActivity.this, "✅ Thêm khách hàng thành công", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(ThemKhachHangActivity.this, "❌ Thêm thất bại (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KhachHang> call, Throwable t) {
                Toast.makeText(ThemKhachHangActivity.this, "🚫 Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
