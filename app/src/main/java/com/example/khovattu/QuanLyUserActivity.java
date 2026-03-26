package com.example.khovattu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.adapter.UserListAdapter;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.UserModel;
import com.example.khovattu.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyUserActivity extends AppCompatActivity {

    private ListView lvUsers;
    private EditText edtSearchUser;
    private Button btnThemNguoiDung, btnDangXuatAdmin;

    private UserListAdapter adapter;
    private List<UserModel> dsNguoiDung = new ArrayList<>();
    private List<UserModel> dsGoc = new ArrayList<>();

    private ApiService api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_user);

        lvUsers = findViewById(R.id.lvUsers);
        edtSearchUser = findViewById(R.id.edtSearchUser);
        btnThemNguoiDung = findViewById(R.id.btnThemNguoiDung);
        btnDangXuatAdmin = findViewById(R.id.btnDangXuatAdmin);

        api = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        adapter = new UserListAdapter(this, dsNguoiDung, sessionManager.getToken(), this::loadDanhSachNguoiDung);
        lvUsers.setAdapter(adapter);

        loadDanhSachNguoiDung();

        btnThemNguoiDung.setOnClickListener(v -> {
            Intent i = new Intent(this, ThemUserActivity.class);
            startActivity(i);
        });

        btnDangXuatAdmin.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString().toLowerCase();
                dsNguoiDung.clear();

                if (key.isEmpty()) dsNguoiDung.addAll(dsGoc);
                else {
                    for (UserModel u : dsGoc) {
                        if (u.getEmail().toLowerCase().contains(key)
                                || u.getUsername().toLowerCase().contains(key)) {
                            dsNguoiDung.add(u);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void loadDanhSachNguoiDung() {
        api.getAllUsers(sessionManager.getToken())
                .enqueue(new Callback<List<UserModel>>() {
                    @Override
                    public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            dsNguoiDung.clear();
                            dsGoc.clear();

                            dsNguoiDung.addAll(response.body());
                            dsGoc.addAll(response.body());

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserModel>> call, Throwable t) {
                        Toast.makeText(QuanLyUserActivity.this,
                                "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
