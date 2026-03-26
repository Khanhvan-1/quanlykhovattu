package com.example.khovattu;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.*;

public class ThemUserActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Spinner spRole;
    Button btnLuuUser;

    ApiService api;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_user);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        spRole = findViewById(R.id.spRole);
        btnLuuUser = findViewById(R.id.btnLuuUser);

        api = ApiClient.getClient(this).create(ApiService.class);
        token = new SessionManager(this).getToken();

        String[] roles = {"admin", "nhap_kho", "xuat_kho"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        btnLuuUser.setOnClickListener(v -> themUser());
    }

    private void themUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("username", name);
        body.put("email", email);
        body.put("password", pass);
        body.put("role", role);

        api.themUser(token, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> res) {
                if (res.isSuccessful()) {
                    Toast.makeText(ThemUserActivity.this, "Thêm user thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ThemUserActivity.this, "Lỗi khi thêm!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ThemUserActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
