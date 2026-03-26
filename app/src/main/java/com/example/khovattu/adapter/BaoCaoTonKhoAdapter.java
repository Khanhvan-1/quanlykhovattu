package com.example.khovattu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.R;
import com.example.khovattu.model.BaoCaoTonKho;

import java.util.List;

public class BaoCaoTonKhoAdapter extends RecyclerView.Adapter<BaoCaoTonKhoAdapter.ViewHolder> {

    private List<BaoCaoTonKho> ds;
    private Context context;

    public BaoCaoTonKhoAdapter(List<BaoCaoTonKho> ds, Context context) {
        this.ds = ds;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ton_kho, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaoCaoTonKho sp = ds.get(position);
        if (sp == null) return;

        holder.tvTenSP.setText("Tên hàng: " + sp.getTenHang());
        holder.tvMaSP.setText("Mã hàng: " + sp.getMaHang());
        holder.tvNhap.setText("Nhập: +" + sp.getTongNhap());
        holder.tvXuat.setText("Xuất: -" + sp.getTongXuat());
        holder.tvTonKho.setText("Tồn kho: " + sp.getTonKho() + " sp");
    }

    @Override
    public int getItemCount() {
        return ds != null ? ds.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenSP, tvMaSP, tvNhap, tvXuat, tvTonKho;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvMaSP = itemView.findViewById(R.id.tvMaSP);
            tvNhap = itemView.findViewById(R.id.tvNhap);
            tvXuat = itemView.findViewById(R.id.tvXuat);
            tvTonKho = itemView.findViewById(R.id.tvTonKho);
        }
    }
}
