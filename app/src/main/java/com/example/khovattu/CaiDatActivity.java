package com.example.khovattu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.utils.SessionManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaiDatActivity extends AppCompatActivity {

    private EditText edtOldPass, edtNewPass, edtConfirmPass;
    private Button btnDoiMatKhau, btnHuy;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        edtOldPass = findViewById(R.id.edtOldPass);
        edtNewPass = findViewById(R.id.edtNewPass);
        edtConfirmPass = findViewById(R.id.edtConfirmPass);
        btnDoiMatKhau = findViewById(R.id.btnDoiMatKhau);
        btnHuy = findViewById(R.id.btnHuy);

        btnDoiMatKhau.setOnClickListener(v -> doiMatKhau());
        btnHuy.setOnClickListener(v -> finish());
    }

    private void doiMatKhau() {
        String oldPass = edtOldPass.getText().toString().trim();
        String newPass = edtNewPass.getText().toString().trim();
        String confirmPass = edtConfirmPass.getText().toString().trim();

        // ✅ Kiểm tra dữ liệu nhập
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Gọi API đổi mật khẩu
        apiService.doiMatKhau("Bearer " + token, oldPass, newPass)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Object msg = response.body().get("message");
                            String message = msg != null ? msg.toString() : "Đổi mật khẩu thành công!";
                            Toast.makeText(CaiDatActivity.this, message, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CaiDatActivity.this, "Đổi mật khẩu thất bại! Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(CaiDatActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
