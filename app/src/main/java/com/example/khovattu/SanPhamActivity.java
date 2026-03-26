package com.example.khovattu;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.khovattu.adapter.SanPhamListAdapter;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SanPhamActivity extends AppCompatActivity {

    private ListView lvSanPham;
    private ImageButton btnAddSP, btnFilterNangCao;
    private EditText edtSearch;
    private Spinner spFilterLoai;

    private SanPhamListAdapter adapter;
    private List<SanPham> sanPhamList = new ArrayList<>();
    private List<SanPham> sanPhamListGoc = new ArrayList<>();

    private ApiService apiService;
    private ActivityResultLauncher<Intent> themSanPhamLauncher;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    private String role, token;

    private List<String> dsLoai = new ArrayList<>();

    // Bộ lọc
    private String fGiaMin = "", fGiaMax = "";
    private String fTonMin = "", fTonMax = "";
    private String fLoaiChon = "Tất cả";
    private String fDateFrom = "", fDateTo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanpham);

        // Ánh xạ
        lvSanPham = findViewById(R.id.lvSanPham);
        btnAddSP = findViewById(R.id.btnAddSP);
        btnFilterNangCao = findViewById(R.id.btnFilterNangCao);
        edtSearch = findViewById(R.id.edtSearch);
        spFilterLoai = findViewById(R.id.spFilterLoai);
        bottomNav = findViewById(R.id.bottomNav);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        role = sessionManager.getRole();   // admin, nhap_kho, xuat_kho
        token = sessionManager.getToken();

        adapter = new SanPhamListAdapter(this, sanPhamList);
        lvSanPham.setAdapter(adapter);

        // Nhận kết quả từ Activity thêm sản phẩm
        themSanPhamLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> { if (result.getResultCode() == RESULT_OK) loadSanPhams(); });

        // ============================
        // 🔐 QUYỀN THÊM SẢN PHẨM
        // ============================
        if ("xuat_kho".equals(role)) {
            btnAddSP.setVisibility(View.GONE);   // Xuất kho không được thêm
        } else {
            btnAddSP.setVisibility(View.VISIBLE);

            btnAddSP.setOnClickListener(v -> {
                // Phòng ngừa bug: xuất kho vẫn bấm được
                if ("xuat_kho".equals(role)) {
                    Toast.makeText(this, "Bạn không có quyền thêm sản phẩm!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(this, ThemSanPhamActivity.class);
                themSanPhamLauncher.launch(intent);
            });
        }

        setupSearchFilter();
        setupFilterLoai();
        setupBottomNavigation();

        loadSanPhams();

        btnFilterNangCao.setOnClickListener(v -> openFilterDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSanPhams();  // refresh
    }

    // ======================= TÌM KIẾM ========================
    private void setupSearchFilter() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                locSanPham();
            }
        });
    }

    // ======================= SPINNER LOẠI ======================
    private void setupFilterLoai() {
        dsLoai.add("Tất cả");

        ArrayAdapter<String> loaiAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dsLoai);

        spFilterLoai.setAdapter(loaiAdapter);

        spFilterLoai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                fLoaiChon = dsLoai.get(pos);
                locSanPham();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ======================= POPUP LỌC NÂNG CAO ======================
    private void openFilterDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_filter_sanpham, null);

        EditText edtGiaMin = view.findViewById(R.id.edtGiaMin);
        EditText edtGiaMax = view.findViewById(R.id.edtGiaMax);
        EditText edtTonMin = view.findViewById(R.id.edtTonMin);
        EditText edtTonMax = view.findViewById(R.id.edtTonMax);
        EditText edtDateFrom = view.findViewById(R.id.edtDateFrom);
        EditText edtDateTo   = view.findViewById(R.id.edtDateTo);
        Spinner spLoaiPopup  = view.findViewById(R.id.spLoaiPopup);

        Button btnApDung = view.findViewById(R.id.btnApDungFilter);
        Button btnXoa    = view.findViewById(R.id.btnXoaFilter);

        ArrayAdapter<String> adapterLoai =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dsLoai);
        spLoaiPopup.setAdapter(adapterLoai);

        spLoaiPopup.setSelection(dsLoai.indexOf(fLoaiChon));

        edtGiaMin.setText(fGiaMin);
        edtGiaMax.setText(fGiaMax);
        edtTonMin.setText(fTonMin);
        edtTonMax.setText(fTonMax);
        edtDateFrom.setText(fDateFrom);
        edtDateTo.setText(fDateTo);

        edtDateFrom.setOnClickListener(v -> showDatePicker(edtDateFrom));
        edtDateTo.setOnClickListener(v -> showDatePicker(edtDateTo));

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        btnApDung.setOnClickListener(v -> {
            fGiaMin = edtGiaMin.getText().toString();
            fGiaMax = edtGiaMax.getText().toString();
            fTonMin = edtTonMin.getText().toString();
            fTonMax = edtTonMax.getText().toString();
            fLoaiChon = spLoaiPopup.getSelectedItem().toString();
            fDateFrom = edtDateFrom.getText().toString();
            fDateTo   = edtDateTo.getText().toString();

            locSanPham();
            dialog.dismiss();
        });

        btnXoa.setOnClickListener(v -> {
            fGiaMin = fGiaMax = fTonMin = fTonMax = fDateFrom = fDateTo = "";
            fLoaiChon = "Tất cả";

            locSanPham();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        target.setText(String.format("%02d-%02d-%04d", day, month + 1, year)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ======================= LỌC SẢN PHẨM ======================
    private void locSanPham() {

        String keyword = edtSearch.getText().toString().trim().toLowerCase();
        List<SanPham> filtered = new ArrayList<>();

        for (SanPham sp : sanPhamListGoc) {

            boolean matchKeyword =
                    sp.getMaHang().toLowerCase().contains(keyword) ||
                            sp.getTenHang().toLowerCase().contains(keyword);

            boolean matchLoai =
                    fLoaiChon.equals("Tất cả") ||
                            (sp.getLoaiHang() != null && sp.getLoaiHang().equalsIgnoreCase(fLoaiChon));

            boolean matchGia = true;
            try {
                if (!fGiaMin.isEmpty() && sp.getGiaNhap() < Double.parseDouble(fGiaMin)) matchGia = false;
                if (!fGiaMax.isEmpty() && sp.getGiaNhap() > Double.parseDouble(fGiaMax)) matchGia = false;
            } catch (Exception ignore) {}

            int ton = sp.getTonKho() > 0 ? sp.getTonKho() : sp.getSoLuong();
            boolean matchTon = true;
            try {
                if (!fTonMin.isEmpty() && ton < Integer.parseInt(fTonMin)) matchTon = false;
                if (!fTonMax.isEmpty() && ton > Integer.parseInt(fTonMax)) matchTon = false;
            } catch (Exception ignore) {}

            if (matchKeyword && matchLoai && matchGia && matchTon) {
                filtered.add(sp);
            }
        }

        sanPhamList.clear();
        sanPhamList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    // ======================= LOAD DANH SÁCH ======================
    private void loadSanPhams() {

        apiService.layDanhSachSanPham(token).enqueue(new Callback<List<SanPham>>() {
            @Override
            public void onResponse(Call<List<SanPham>> call, Response<List<SanPham>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    sanPhamList.clear();
                    sanPhamListGoc.clear();

                    sanPhamList.addAll(response.body());
                    sanPhamListGoc.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    // =================== CẢNH BÁO TỒN KHO ===================
                    List<SanPham> lowStockList = new ArrayList<>();

                    for (SanPham sp : sanPhamListGoc) {
                        int ton = sp.getTonKho() > 0 ? sp.getTonKho() : sp.getSoLuong();
                        if (ton < 10) lowStockList.add(sp);
                    }

                    if (!lowStockList.isEmpty()) {

                        // popup
                        StringBuilder builder = new StringBuilder("⚠ Các sản phẩm tồn kho thấp:\n\n");
                        StringBuilder notifyMsg = new StringBuilder();

                        for (SanPham sp : lowStockList) {
                            int ton = sp.getTonKho() > 0 ? sp.getTonKho() : sp.getSoLuong();
                            builder.append("• ").append(sp.getTenHang()).append(" — ").append(ton).append("\n");
                            notifyMsg.append(sp.getTenHang()).append(" — ").append(ton).append("\n");
                        }

                        builder.append("\nVui lòng bổ sung hàng!");

                        new AlertDialog.Builder(SanPhamActivity.this)
                                .setTitle("⚠ Cảnh báo tồn kho")
                                .setMessage(builder.toString())
                                .setPositiveButton("OK", null)
                                .show();

                        // notificaton
                        showLowStockNotification(notifyMsg.toString());
                    }

                    // Update loại hàng
                    Set<String> uniqueLoai = new HashSet<>();
                    uniqueLoai.add("Tất cả");

                    for (SanPham sp : sanPhamListGoc) {
                        if (sp.getLoaiHang() != null && !sp.getLoaiHang().isEmpty())
                            uniqueLoai.add(sp.getLoaiHang());
                    }

                    dsLoai.clear();
                    dsLoai.addAll(uniqueLoai);

                    ((ArrayAdapter) spFilterLoai.getAdapter()).notifyDataSetChanged();

                    locSanPham();
                }
            }

            @Override
            public void onFailure(Call<List<SanPham>> call, Throwable t) {
                Toast.makeText(SanPhamActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ======================= BOTTOM NAV ======================
    private void setupBottomNavigation() {

        bottomNav.setSelectedItemId(R.id.nav_sanpham);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home)
                startActivity(new Intent(this, MainActivity.class));

            else if (item.getItemId() == R.id.nav_lichsu)
                startActivity(new Intent(this, LichSuActivity.class));

            else if (item.getItemId() == R.id.nav_khac)
                startActivity(new Intent(this, MenuKhacActivity.class));

            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

    // ======================= NOTIFICATION ======================
    private void showLowStockNotification(String message) {

        String CHANNEL_ID = "low_stock_channel";

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Cảnh báo tồn kho", NotificationManager.IMPORTANCE_HIGH);

            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    attrs
            );

            channel.enableLights(true);
            channel.enableVibration(true);

            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_warning)
                        .setContentTitle("⚠ Cảnh báo tồn kho thấp")
                        .setContentText("Có sản phẩm dưới mức tồn kho!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{300, 300, 300})
                        .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
