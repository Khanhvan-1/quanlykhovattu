package com.example.khovattu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.LoginRequest;
import com.example.khovattu.model.LoginResponse;
import com.example.khovattu.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUser, edtPass;
    private Button btnLogin, btnToRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());

        // Nếu đã đăng nhập → điều hướng luôn
        if (sessionManager.isLoggedIn()) {
            directByRole(sessionManager.getRole());
            return;
        }

        edtUser = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.btnToRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = edtUser.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();

                    if (res.getToken() == null || res.getUser() == null) {
                        Toast.makeText(LoginActivity.this, "Phản hồi không hợp lệ từ máy chủ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = res.getUser().getRole().toLowerCase();
                    String maNV = res.getUser().getMaNV();

                    sessionManager.saveLoginFull(
                            res.getToken(),
                            res.getUser().getUsername(),
                            res.getUser().getEmail(),
                            role,
                            maNV
                    );

                    Toast.makeText(LoginActivity.this,
                            "Đăng nhập thành công (" + role + ")",
                            Toast.LENGTH_SHORT).show();

                    directByRole(role);

                } else {
                    Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void directByRole(String role) {
        Intent intent;

        switch (role) {
            case "admin":
                intent = new Intent(this, MainActivity.class);
                break;

            case "nhap_kho":
                intent = new Intent(this, NhapKhoActivity.class);
                break;

            case "xuat_kho":
                intent = new Intent(this, XuatKhoActivity.class);
                break;

            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }

        startActivity(intent);
        finish();
    }
}
