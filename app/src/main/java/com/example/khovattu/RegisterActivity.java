package com.example.khovattu;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.RegisterRequest;
import com.example.khovattu.model.RegisterResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUser, edtEmail, edtPass;
    private Spinner spinnerGender;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUser = findViewById(R.id.edtUser);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);

        // Spinner giới tính
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> {
            String username = edtUser.getText().toString().trim();
            String email = edtEmail.getText().toString().trim().toLowerCase();
            String password = edtPass.getText().toString().trim();

            // ⭐ FIX CHUẨN GIỚI TÍNH TRÙNG BACKEND
            String gioiTinh = spinnerGender.getSelectedItem().toString().trim();
            if (gioiTinh.equalsIgnoreCase("Nữ") || gioiTinh.equalsIgnoreCase("Nu")) {
                gioiTinh = "Nữ";
            } else {
                gioiTinh = "Nam";
            }

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest(username, email, password, gioiTinh);
            ApiService api = ApiClient.getClient(this).create(ApiService.class);

            api.register(request).enqueue(new Callback<RegisterResponse>() {
                @Override
                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        if (response.body().isSuccess()) {
                            Toast.makeText(RegisterActivity.this,
                                    "🎉 " + response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "❌ " + response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        try {
                            String err = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Không xác định";

                            Toast.makeText(RegisterActivity.this, "❌ " + err, Toast.LENGTH_LONG).show();
                            Log.e("REGISTER_API", err);
                        } catch (IOException ignored) {}
                    }
                }

                @Override
                public void onFailure(Call<RegisterResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this,
                            "🚫 Không thể kết nối máy chủ: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
