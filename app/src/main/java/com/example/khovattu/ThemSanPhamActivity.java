package com.example.khovattu;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemSanPhamActivity extends AppCompatActivity {

    private EditText edtMaSP, edtTenSP, edtGiaNhap, edtGhiChu, edtPhanLoai;
    private Button btnLuu, btnLuuTaoMoi;

    private ApiService apiService;
    private SessionManager sessionManager;

    private boolean isEditMode = false;
    private SanPham sanPhamHienTai;

    private String role, token;

    // --------------------- KEYWORD MAP ---------------------
    private final Map<String, String> keywordMap = new HashMap<String, String>() {{
        put("maipin", "Máy mài pin");
        put("khoanpin", "Máy khoan pin");
        put("khoantu", "Máy khoan pin");
        put("khoantudo", "Máy khoan pin");
        put("moocbin", "Máy mở ốc pin");
        put("sietbulong", "Máy siết bulông");
        put("mooc", "Máy mở ốc");
        put("mai", "Máy mài");
        put("khoan", "Máy khoan");
        put("cua", "Máy cưa");
        put("catgo", "Máy cắt gỗ");
        put("han", "Máy hàn");
        put("cualong", "Máy cưa lọng");
        put("tiahangrao", "Máy tỉa hàng rào");
        put("catcanh", "Máy cắt cành");
        put("catla", "Máy cắt lá");
        put("duc", "Máy đục");
        put("muikhoan", "Mũi khoan");
        put("muiduc", "Mũi đục");
        put("toi", "Máy tời");

        put("mayruaxe", "Máy rửa xe");
        put("ruaxe", "Máy rửa xe");
        put("mayruaxehonda", "Máy rửa xe");

        put("ongnuoc", "Dụng cụ nước");
        put("voixit", "Dụng cụ nước");
        put("dayruaxe", "Dụng cụ nước");
        put("sungruaxe", "Dụng cụ nước");

        put("bulong", "Bulong & ốc vít");
        put("son", "Máy phun sơn");
        put("catsat", "Máy cắt sắt");
        put("phukien", "Phụ kiện");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_san_pham);

        initViews();

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        role = sessionManager.getRole();
        token = sessionManager.getToken();

        // ------------ CHECK EDIT MODE ------------
        if (getIntent().hasExtra("SAN_PHAM_DE_SUA")) {
            isEditMode = true;
            sanPhamHienTai = getIntent().getParcelableExtra("SAN_PHAM_DE_SUA");
            dienThongTinDeSua();
        }

        if (isEditMode && !"admin".equalsIgnoreCase(role)) {
            Toast.makeText(this, "🚫 Bạn không có quyền sửa!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isEditMode) {
            setTitle("Cập nhật sản phẩm");
            btnLuuTaoMoi.setVisibility(Button.GONE);
            edtMaSP.setEnabled(false);
        } else {
            setTitle("Thêm sản phẩm mới");
            autoClassifySetup();
        }

        btnLuu.setOnClickListener(v -> luuSanPham(false));
        btnLuuTaoMoi.setOnClickListener(v -> luuSanPham(true));
    }

    private void initViews() {
        edtMaSP = findViewById(R.id.edtMaSP);
        edtTenSP = findViewById(R.id.edtTenSP);
        edtGiaNhap = findViewById(R.id.edtGiaVon);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        edtPhanLoai = findViewById(R.id.edtPhanLoai);
        btnLuu = findViewById(R.id.btnLuu);
        btnLuuTaoMoi = findViewById(R.id.btnLuuTaoMoi);
    }

    // ------------------ AUTO CLASSIFY ------------------
    private void autoClassifySetup() {

        edtTenSP.addTextChangedListener(new TextWatcher() {

            private android.os.Handler handler = new android.os.Handler();
            private Runnable runnable;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (runnable != null) handler.removeCallbacks(runnable);

                runnable = () -> {
                    String name = edtTenSP.getText().toString().trim();
                    if (name.isEmpty()) {
                        edtPhanLoai.setText("Khác");
                    } else {
                        edtPhanLoai.setText(classifyProduct(name));
                    }
                };

                // ⏳ 200ms sau khi người dùng dừng nhập → chạy phân loại
                handler.postDelayed(runnable, 200);
            }
        });
    }


    // ------------------ FILL DATA EDIT ------------------
    private void dienThongTinDeSua() {
        edtMaSP.setText(sanPhamHienTai.getMaHang());
        edtTenSP.setText(sanPhamHienTai.getTenHang());
        edtGiaNhap.setText(String.valueOf(sanPhamHienTai.getGiaNhap()));
        edtGhiChu.setText(sanPhamHienTai.getGhiChu());
        edtPhanLoai.setText(sanPhamHienTai.getLoaiHang());
    }

    // ------------------ SAVE PRODUCT ------------------
    private void luuSanPham(boolean taoMoi) {

        String ma = edtMaSP.getText().toString().trim();
        String ten = edtTenSP.getText().toString().trim();
        String loai = edtPhanLoai.getText().toString().trim();
        String giaNhapStr = edtGiaNhap.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            Toast.makeText(this, "⚠️ Nhập Mã + Tên sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        double giaNhap = 0;
        try {
            if (!giaNhapStr.isEmpty()) giaNhap = Double.parseDouble(giaNhapStr);
        } catch (Exception e) {
            Toast.makeText(this, "❌ Giá nhập không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        SanPham sp = new SanPham();
        sp.setMaHang(ma);
        sp.setTenHang(ten);
        sp.setGiaNhap(giaNhap);
        sp.setGiaXuat(giaNhap);
        sp.setGhiChu(ghiChu);

        // 🎯 Gửi loại đúng với UI
        sp.setLoaiHang(loai);

        Call<SanPham> call = isEditMode
                ? apiService.capNhatSanPham(token, sanPhamHienTai.getId(), sp)
                : apiService.themSanPham(token, sp);

        call.enqueue(new Callback<SanPham>() {
            @Override
            public void onResponse(Call<SanPham> call, Response<SanPham> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(ThemSanPhamActivity.this,
                            isEditMode ? "✔ Cập nhật thành công!" : "✔ Thêm thành công!",
                            Toast.LENGTH_SHORT).show();

                    setResult(Activity.RESULT_OK);
                    finish();

                } else {
                    Toast.makeText(ThemSanPhamActivity.this,
                            "❌ Lỗi! Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SanPham> call, Throwable t) {
                Toast.makeText(ThemSanPhamActivity.this,
                        "🚫 Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------ LOCAL CLASSIFY ------------------
    private String classifyProduct(String name) {

        String clean = removeVietnameseTones(name)
                .toLowerCase()
                .replaceAll("\\s+", "");

        for (Map.Entry<String, String> e : keywordMap.entrySet()) {
            if (clean.contains(e.getKey())) return e.getValue();
        }

        return "Khác";
    }

    private String removeVietnameseTones(String str) {
        return str.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴàáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄèéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ÌÍỊỈĨìíịỉĩ]", "i")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠòóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮùúụủũưừứựửữ]", "u")
                .replaceAll("[ỲÝỴỶỸỳýỵỷỹ]", "y")
                .replaceAll("[Đđ]", "d");
    }
}
