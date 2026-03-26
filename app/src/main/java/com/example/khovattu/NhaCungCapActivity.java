package com.example.khovattu;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.adapter.NhaCungCapAdapter;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.DialogNCC;
import com.example.khovattu.model.NhaCungCap;
import com.example.khovattu.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NhaCungCapActivity extends AppCompatActivity {

    RecyclerView rv;
    ImageButton btnBack, btnAdd;
    ArrayList<NhaCungCap> list = new ArrayList<>();
    ApiService api;
    SessionManager sm;
    String token;
    NhaCungCapAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ncc);

        rv = findViewById(R.id.recyclerNCC);
        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAddNCC);

        api = ApiClient.getClient(this).create(ApiService.class);
        sm = new SessionManager(this);
        token = sm.getToken();

        // ⚠️ Check token hết hạn
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // BẮT BUỘC: set layout manager
        rv.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showDialogThemNCC());

        loadData();
    }

    private void loadData() {
        api.layDanhSachNCC(token).enqueue(new Callback<List<NhaCungCap>>() {
            @Override
            public void onResponse(Call<List<NhaCungCap>> call, Response<List<NhaCungCap>> res) {
                if (res.isSuccessful() && res.body() != null) {

                    list.clear();
                    list.addAll(res.body());

                    if (adapter == null) {
                        adapter = new NhaCungCapAdapter(
                                NhaCungCapActivity.this,
                                list,
                                new NhaCungCapAdapter.OnNCCAction() {

                                    @Override
                                    public void onEdit(NhaCungCap n) {
                                        showDialogSuaNCC(n);
                                    }

                                    @Override
                                    public void onDelete(NhaCungCap n) {
                                        xoaNCC(n);
                                    }
                                });

                        rv.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                } else {
                    Toast.makeText(NhaCungCapActivity.this,
                            "Không tải được danh sách NCC!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<NhaCungCap>> call, Throwable t) {
                Toast.makeText(NhaCungCapActivity.this,
                        "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogThemNCC() {
        DialogNCC dialog = new DialogNCC(this, null, (ncc, isEdit) -> {

            api.themNCC(token, ncc).enqueue(new Callback<NhaCungCap>() {
                @Override
                public void onResponse(Call<NhaCungCap> call, Response<NhaCungCap> res) {
                    Toast.makeText(NhaCungCapActivity.this,
                            "Thêm nhà cung cấp thành công!", Toast.LENGTH_SHORT).show();
                    loadData();
                }

                @Override
                public void onFailure(Call<NhaCungCap> call, Throwable t) {
                    Toast.makeText(NhaCungCapActivity.this,
                            "Lỗi thêm NCC!", Toast.LENGTH_SHORT).show();
                }
            });

        });

        dialog.show();
    }

    private void showDialogSuaNCC(NhaCungCap ncc) {
        DialogNCC dialog = new DialogNCC(this, ncc, (updated, isEdit) -> {

            api.capNhatNCC(token, ncc.getId(), updated).enqueue(new Callback<NhaCungCap>() {
                @Override
                public void onResponse(Call<NhaCungCap> call, Response<NhaCungCap> res) {
                    Toast.makeText(NhaCungCapActivity.this,
                            "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    loadData();
                }

                @Override
                public void onFailure(Call<NhaCungCap> call, Throwable t) {
                    Toast.makeText(NhaCungCapActivity.this,
                            "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                }
            });

        });

        dialog.show();
    }

    private void xoaNCC(NhaCungCap n) {

        api.xoaNCC(token, n.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                Toast.makeText(NhaCungCapActivity.this,
                        "Đã xoá nhà cung cấp!", Toast.LENGTH_SHORT).show();
                loadData();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NhaCungCapActivity.this,
                        "Lỗi xoá NCC!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
