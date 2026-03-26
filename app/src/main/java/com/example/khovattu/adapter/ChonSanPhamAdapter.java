package com.example.khovattu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.khovattu.R;
import com.example.khovattu.model.SanPham;
import java.text.DecimalFormat;
import java.util.*;

public class ChonSanPhamAdapter extends RecyclerView.Adapter<ChonSanPhamAdapter.ViewHolder> {

    private final List<SanPham> sanPhamList;
    private final Context context;
    private final Map<String, SanPham> selected = new HashMap<>();

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int tongSP, double tongGia);
    }
    private OnSelectionChangedListener listener;
    public void setOnSelectionChangedListener(OnSelectionChangedListener l) { listener = l; }

    public ChonSanPhamAdapter(List<SanPham> list, Context ctx) {
        sanPhamList = list;
        context = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sanpham, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        SanPham sp = sanPhamList.get(pos);
        DecimalFormat fmt = new DecimalFormat("###,###,###");
        h.tvTen.setText(sp.getTenHang());
        h.tvMa.setText("Mã: " + sp.getMaHang());
        h.tvGia.setText("Giá nhập: " + fmt.format(sp.getGiaNhap()) + " ₫");
        h.tvSoLuong.setText("Tồn: " + sp.getSoLuong());

        h.itemView.setBackgroundResource(selected.containsKey(sp.getId())
                ? R.color.selected_item_background : android.R.color.transparent);

        h.itemView.setOnClickListener(v -> showDialog(sp, pos));
    }

    @Override
    public int getItemCount() { return sanPhamList.size(); }

    private void showDialog(SanPham sp, int pos) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_xuat_soluong, null);
        AlertDialog d = new AlertDialog.Builder(context).setView(v).create();

        TextView tvTen = v.findViewById(R.id.tvTenSP);
        TextView tvGia = v.findViewById(R.id.tvGiaLe);
        EditText edtSoLuong = v.findViewById(R.id.edtSoLuong);
        Button btnTru = v.findViewById(R.id.btnTru);
        Button btnCong = v.findViewById(R.id.btnCong);
        Button btnGui = v.findViewById(R.id.btnGui);
        Button btnHuy = v.findViewById(R.id.btnHuy);

        tvTen.setText(sp.getTenHang());
        tvGia.setText("Giá nhập: " + new DecimalFormat("###,###").format(sp.getGiaNhap()) + " ₫");
        edtSoLuong.setText("1");

        btnCong.setOnClickListener(x -> {
            int sl = Integer.parseInt(edtSoLuong.getText().toString());
            edtSoLuong.setText(String.valueOf(sl + 1));
        });
        btnTru.setOnClickListener(x -> {
            int sl = Integer.parseInt(edtSoLuong.getText().toString());
            if (sl > 1) edtSoLuong.setText(String.valueOf(sl - 1));
        });

        btnGui.setOnClickListener(x -> {
            int soLuong = Integer.parseInt(edtSoLuong.getText().toString());

            if (soLuong <= 0) {
                Toast.makeText(context,
                        "Số lượng phải lớn hơn 0!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            SanPham spChon = new SanPham();
            spChon.setId(sp.getId());
            spChon.setMaHang(sp.getMaHang());
            spChon.setTenHang(sp.getTenHang());
            spChon.setSoLuong(soLuong);
            spChon.setGiaNhap(sp.getGiaNhap());
            spChon.setGiaXuat(sp.getGiaNhap());

            selected.put(sp.getId(), spChon);

            if (listener != null) {
                int tong = 0;
                double gia = 0;
                for (SanPham s : selected.values()) {
                    tong += s.getSoLuong();
                    gia += s.getGiaNhap() * s.getSoLuong();
                }
                listener.onSelectionChanged(tong, gia);
            }
            notifyItemChanged(pos);
            d.dismiss();
        });

        btnHuy.setOnClickListener(x -> d.dismiss());
        d.show();
    }

    public ArrayList<SanPham> getDanhSachDaChon() {
        return new ArrayList<>(selected.values());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvMa, tvGia, tvSoLuong;
        ViewHolder(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTenSP);
            tvMa = v.findViewById(R.id.tvMaSP);
            tvGia = v.findViewById(R.id.tvGiaSP);
            tvSoLuong = v.findViewById(R.id.tvSoLuong);
        }
    }
}
