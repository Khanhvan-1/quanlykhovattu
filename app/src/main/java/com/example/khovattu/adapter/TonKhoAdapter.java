package com.example.khovattu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.R;

import java.util.HashMap;
import java.util.List;

public class TonKhoAdapter extends RecyclerView.Adapter<TonKhoAdapter.TonKhoViewHolder> {

    private List<HashMap<String, String>> tonKhoList;
    private Context context;

    public TonKhoAdapter(List<HashMap<String, String>> tonKhoList, Context context) {
        this.tonKhoList = tonKhoList;
        this.context = context;
    }

    @NonNull
    @Override
    public TonKhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ton_kho, parent, false);
        return new TonKhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TonKhoViewHolder holder, int position) {
        HashMap<String, String> item = tonKhoList.get(position);
        if (item == null) return;

        holder.tvTenSP.setText(item.get("tenHang"));
        holder.tvMaSP.setText(item.get("maHang"));
        holder.tvNhap.setText(item.get("nhap"));
        holder.tvXuat.setText(item.get("xuat"));
        holder.tvTonKho.setText(item.get("tonKho"));
    }

    @Override
    public int getItemCount() {
        return tonKhoList != null ? tonKhoList.size() : 0;
    }

    public static class TonKhoViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenSP, tvMaSP, tvNhap, tvXuat, tvTonKho;

        public TonKhoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvMaSP = itemView.findViewById(R.id.tvMaSP);
            tvNhap = itemView.findViewById(R.id.tvNhap);
            tvXuat = itemView.findViewById(R.id.tvXuat);
            tvTonKho = itemView.findViewById(R.id.tvTonKho);
        }
    }
}
