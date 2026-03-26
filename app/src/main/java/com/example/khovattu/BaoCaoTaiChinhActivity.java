package com.example.khovattu;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.BaoCaoResponse;
import com.example.khovattu.model.BieuDoNhapXuat;
import com.example.khovattu.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaoCaoTaiChinhActivity extends AppCompatActivity {

    private BarChart barChart;
    private PieChart pieChart;
    private LineChart lineChartNhapXuat;

    private TextView tvDoanhThu, tvChiPhi;
    private ImageButton btnBack;
    private Button btnRefresh, btnFilter, btnExport;

    private EditText edtFrom, edtTo;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String token;

    private final SimpleDateFormat sdfSend = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfShow = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao_taichinh);

        initViews();

        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();

        btnBack.setOnClickListener(v -> finish());
        edtFrom.setOnClickListener(v -> pickDate(edtFrom));
        edtTo.setOnClickListener(v -> pickDate(edtTo));

        btnFilter.setOnClickListener(v -> filterData());
        btnRefresh.setOnClickListener(v -> loadData());
        btnExport.setOnClickListener(v -> exportExcel());

        loadData();
    }

    private void initViews() {
        barChart = findViewById(R.id.barChartDoanhThuChiPhi);
        pieChart = findViewById(R.id.pieChart);
        lineChartNhapXuat = findViewById(R.id.lineChartNhapXuat);

        tvDoanhThu = findViewById(R.id.tvDoanhThu);
        tvChiPhi = findViewById(R.id.tvChiPhi);

        edtFrom = findViewById(R.id.edtFromDate);
        edtTo = findViewById(R.id.edtToDate);

        btnFilter = findViewById(R.id.btnLoc);
        btnExport = findViewById(R.id.btnExportExcel);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnBack = findViewById(R.id.btnBack);
    }

    private void pickDate(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(y, m, d);
            target.setText(sdfShow.format(cal.getTime()));
            target.setTag(sdfSend.format(cal.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadData() {
        apiService.layBaoCaoTongHop(token).enqueue(new Callback<BaoCaoResponse>() {
            @Override
            public void onResponse(Call<BaoCaoResponse> call, Response<BaoCaoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaoCaoResponse data = response.body();
                    updateUI(data);
                    setupBarChart(data);
                    setupPieChart(data);
                }
            }

            @Override
            public void onFailure(Call<BaoCaoResponse> call, Throwable t) {
                Toast.makeText(BaoCaoTaiChinhActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        loadNhapXuat(null, null);
    }

    private void filterData() {
        String from = (String) edtFrom.getTag();
        String to = (String) edtTo.getTag();

        if (from == null || to == null) {
            Toast.makeText(this, "Vui lòng chọn đủ ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadNhapXuat(from, to);
    }

    private void exportExcel() {

        apiService.exportExcel(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(BaoCaoTaiChinhActivity.this,
                            "❌ Xuất Excel thất bại!", Toast.LENGTH_LONG).show();
                    return;
                }

                new Thread(() -> {
                    try {
                        String fileName = "BaoCaoTaiChinh_" + System.currentTimeMillis() + ".xlsx";


                        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        if (!downloads.exists()) downloads.mkdirs();

                        File outFile = new File(downloads, fileName);


                        InputStream in = response.body().byteStream();
                        OutputStream out = new FileOutputStream(outFile);

                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }

                        out.flush();
                        out.close();
                        in.close();

                        runOnUiThread(() -> {
                            Toast.makeText(
                                    BaoCaoTaiChinhActivity.this,
                                    "📁 File Excel đã lưu:\n" + outFile.getAbsolutePath(),
                                    Toast.LENGTH_LONG
                            ).show();


                            try {
                                Intent open = new Intent(Intent.ACTION_VIEW);
                                open.setDataAndType(
                                        FileProvider.getUriForFile(
                                                BaoCaoTaiChinhActivity.this,
                                                getPackageName() + ".provider",
                                                outFile
                                        ),
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                );
                                open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(open, "Mở bằng..."));

                            } catch (Exception e) {
                                Toast.makeText(BaoCaoTaiChinhActivity.this,
                                        "⚠ File đã lưu nhưng không tìm thấy app mở Excel!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        BaoCaoTaiChinhActivity.this,
                                        "❌ Lỗi lưu file: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BaoCaoTaiChinhActivity.this,
                        "⚠ Lỗi mạng: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }



    private void updateUI(BaoCaoResponse b) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvDoanhThu.setText(fmt.format(b.getDoanhThu()));
        tvChiPhi.setText(fmt.format(b.getChiPhi()));
    }

    private void setupBarChart(BaoCaoResponse b) {
        barChart.clear();

        // 1️⃣ Tạo dataset riêng cho Doanh thu
        ArrayList<BarEntry> doanhThuEntry = new ArrayList<>();
        doanhThuEntry.add(new BarEntry(0, (float) b.getDoanhThu()));

        BarDataSet dsDoanhThu = new BarDataSet(doanhThuEntry, "Doanh thu");
        dsDoanhThu.setColor(Color.parseColor("#4CAF50")); // xanh
        dsDoanhThu.setValueTextSize(14f);

        // 2️⃣ Dataset riêng cho Chi phí
        ArrayList<BarEntry> chiPhiEntry = new ArrayList<>();
        chiPhiEntry.add(new BarEntry(1, (float) b.getChiPhi()));

        BarDataSet dsChiPhi = new BarDataSet(chiPhiEntry, "Chi phí");
        dsChiPhi.setColor(Color.parseColor("#FF80D8")); // hồng
        dsChiPhi.setValueTextSize(14f);

        // 3️⃣ Gộp 2 dataset
        BarData barData = new BarData(dsDoanhThu, dsChiPhi);
        barData.setBarWidth(0.35f);

        barChart.setData(barData);

        // 4️⃣ Trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Doanh thu", "Chi phí"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        // 5️⃣ Legend tự hiển thị đúng 2 màu
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(13f);
        legend.setForm(Legend.LegendForm.CIRCLE);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(900);
        barChart.invalidate();
    }


    private void setupPieChart(BaoCaoResponse b) {
        pieChart.clear();
        pieChart.setUsePercentValues(true);

        float doanhThu = (float) b.getDoanhThu();
        float chiPhi = (float) b.getChiPhi();
        float tong = doanhThu + chiPhi;
        if (tong == 0) tong = 1;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(doanhThu / tong * 100f, "Doanh thu"));
        entries.add(new PieEntry(chiPhi / tong * 100f, "Chi phí"));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(Color.parseColor("#9ACBFF"), Color.parseColor("#F7A9FF"));
        ds.setValueTextSize(14f);
        ds.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(new PieData(ds));
        pieChart.setCenterText("TỶ LỆ (%)");
        pieChart.setCenterTextSize(16f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.animateXY(1100, 1100);
    }

    private void loadNhapXuat(String from, String to) {
        apiService.layBieuDoNhapXuat(token, from, to).enqueue(new Callback<List<BieuDoNhapXuat>>() {
            @Override
            public void onResponse(Call<List<BieuDoNhapXuat>> call, Response<List<BieuDoNhapXuat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupLineChartNhapXuat(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<BieuDoNhapXuat>> call, Throwable t) {
                Toast.makeText(BaoCaoTaiChinhActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLineChartNhapXuat(List<BieuDoNhapXuat> list) {
        lineChartNhapXuat.clear();

        List<Entry> nhap = new ArrayList<>();
        List<Entry> xuat = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        float maxY = 0;

        for (int i = 0; i < list.size(); i++) {
            BieuDoNhapXuat b = list.get(i);

            nhap.add(new Entry(i, (float) b.getTongNhap()));
            xuat.add(new Entry(i, (float) b.getTongXuat()));

            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(b.getNgay());
                labels.add(sdfLabel.format(date));
            } catch (Exception e) {
                labels.add(b.getNgay());
            }

            maxY = Math.max(maxY, Math.max((float) b.getTongNhap(), (float) b.getTongXuat()));
        }

        LineDataSet setN = new LineDataSet(nhap, "Nhập");
        setN.setColor(Color.parseColor("#2B7CFF"));
        setN.setCircleColor(Color.parseColor("#2B7CFF"));
        setN.setLineWidth(2.5f);
        setN.setCircleRadius(4f);

        LineDataSet setX = new LineDataSet(xuat, "Xuất");
        setX.setColor(Color.parseColor("#FF6B6B"));
        setX.setCircleColor(Color.parseColor("#FF6B6B"));
        setX.setLineWidth(2.5f);
        setX.setCircleRadius(4f);

        lineChartNhapXuat.setData(new LineData(setN, setX));

        XAxis xAxis = lineChartNhapXuat.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChartNhapXuat.getAxisLeft().setAxisMinimum(0f);
        lineChartNhapXuat.getAxisLeft().setAxisMaximum(maxY * 1.15f);
        lineChartNhapXuat.getAxisRight().setEnabled(false);

        lineChartNhapXuat.animateY(1200);
        lineChartNhapXuat.invalidate();
    }
}
