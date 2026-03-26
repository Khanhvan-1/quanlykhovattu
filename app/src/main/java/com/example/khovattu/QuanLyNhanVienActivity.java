package com.example.khovattu;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.adapter.NhanVienAdapter;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.NhanVien;
import com.example.khovattu.model.NhanVienResponse;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuanLyNhanVienActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private TabLayout tabLayout;

    private NhanVienAdapter adapter;
    private ApiService api;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_nhanvien);

        session = new SessionManager(this);

        // 🚫 CHẶN QUYỀN: chỉ admin được vào trang này
        if (!"admin".equals(session.getRole())) {
            Toast.makeText(this, "❌ Bạn không có quyền truy cập trang nhân viên!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerNhanVien);
        fabAdd = findViewById(R.id.fabAddNhanVien);
        tabLayout = findViewById(R.id.tabNhanVien);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        api = ApiClient.getClient(this).create(ApiService.class);

        setupTabs();
        reloadCurrentTab();  // load mặc định

        fabAdd.setOnClickListener(v -> moDialogThemNhanVien());
    }


    // -----------------------------
    //      SETUP TAB
    // -----------------------------
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Admin"));
        tabLayout.addTab(tabLayout.newTab().setText("Nhập kho"));
        tabLayout.addTab(tabLayout.newTab().setText("Xuất kho"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { reloadCurrentTab(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { reloadCurrentTab(); }
        });
    }


    // -----------------------------
    //      LOAD THEO TAB
    // -----------------------------
    private void reloadCurrentTab() {
        int pos = tabLayout.getSelectedTabPosition();

        switch (pos) {
            case 1: locTheoRole("admin"); break;
            case 2: locTheoRole("nhap_kho"); break;
            case 3: locTheoRole("xuat_kho"); break;
            default: loadTatCa();
        }
    }


    // -----------------------------
    //      TẢI TẤT CẢ
    // -----------------------------
    private void loadTatCa() {
        api.layDanhSachNhanVien(session.getToken())
                .enqueue(new Callback<List<NhanVien>>() {
                    @Override
                    public void onResponse(Call<List<NhanVien>> call, Response<List<NhanVien>> resp) {
                        if (resp.isSuccessful() && resp.body() != null)
                            setAdapter(resp.body());
                        else
                            Toast.makeText(QuanLyNhanVienActivity.this, "Không tải được danh sách!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<List<NhanVien>> call, Throwable t) {
                        Toast.makeText(QuanLyNhanVienActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // -----------------------------
    //     LỌC THEO CHỨC VỤ
    // -----------------------------
    private void locTheoRole(String role) {
        api.locNhanVienTheoRole(session.getToken(), role)
                .enqueue(new Callback<List<NhanVien>>() {
                    @Override
                    public void onResponse(Call<List<NhanVien>> call, Response<List<NhanVien>> resp) {
                        if (resp.isSuccessful() && resp.body() != null)
                            setAdapter(resp.body());
                    }

                    @Override
                    public void onFailure(Call<List<NhanVien>> call, Throwable t) {
                        Toast.makeText(QuanLyNhanVienActivity.this, "Lỗi lọc nhân viên!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // -----------------------------
    //      GẮN ADAPTER
    // -----------------------------
    private void setAdapter(List<NhanVien> ds) {
        adapter = new NhanVienAdapter(ds, this, api, session);
        adapter.setOnDataChangedListener(this::reloadCurrentTab);
        recyclerView.setAdapter(adapter);
    }


    // -----------------------------
    //   DIALOG THÊM NHÂN VIÊN
    // -----------------------------
    private void moDialogThemNhanVien() {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_them_nhanvien, null);

        EditText edtTen = view.findViewById(R.id.edtTenNV);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPass = view.findViewById(R.id.edtMatKhau);
        EditText edtTuoi = view.findViewById(R.id.edtTuoi);
        Spinner spnGioiTinh = view.findViewById(R.id.spnGioiTinh);
        Spinner spnChucVu = view.findViewById(R.id.spnChucVu);

        new AlertDialog.Builder(this)
                .setTitle("Thêm nhân viên mới")
                .setView(view)
                .setPositiveButton("Thêm", (d, w) -> {

                    String ten = edtTen.getText().toString().trim();
                    String email = edtEmail.getText().toString().trim().toLowerCase();
                    String pass = edtPass.getText().toString().trim();
                    String gioiTinh = spnGioiTinh.getSelectedItem().toString();

                    int tuoi;
                    try {
                        tuoi = Integer.parseInt(edtTuoi.getText().toString());
                    } catch (Exception e) {
                        Toast.makeText(this, "Tuổi không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ten.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(this, "Không được bỏ trống!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String chucVu = spnChucVu.getSelectedItem().toString();
                    if (chucVu.contains("Admin")) chucVu = "admin";
                    else if (chucVu.contains("Nhập")) chucVu = "nhap_kho";
                    else chucVu = "xuat_kho";

                    NhanVien nv = new NhanVien();
                    nv.setTenNV(ten);
                    nv.setEmail(email);
                    nv.setPassword(pass);
                    nv.setTuoi(tuoi);
                    nv.setGioiTinh(gioiTinh);
                    nv.setChucVu(chucVu);

                    themNhanVien(nv);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    // -----------------------------
    //   GỌI API THÊM NHÂN VIÊN
    // -----------------------------
    private void themNhanVien(NhanVien nv) {

        api.themNhanVien(session.getToken(), nv)
                .enqueue(new Callback<NhanVienResponse>() {
                    @Override
                    public void onResponse(Call<NhanVienResponse> call, Response<NhanVienResponse> resp) {
                        if (resp.isSuccessful()) {
                            Toast.makeText(QuanLyNhanVienActivity.this,
                                    "Đã thêm nhân viên!", Toast.LENGTH_SHORT).show();
                            reloadCurrentTab();
                        } else {
                            Toast.makeText(QuanLyNhanVienActivity.this, "Thêm thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<NhanVienResponse> call, Throwable t) {
                        Toast.makeText(QuanLyNhanVienActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
