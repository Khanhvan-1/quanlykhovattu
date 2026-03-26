package com.example.khovattu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.khovattu.R;
import com.example.khovattu.model.KhachHang;
import java.util.List;

public class KhachHangAdapter extends RecyclerView.Adapter<KhachHangAdapter.KhachHangViewHolder> {

    private List<KhachHang> khachHangList;
    private Context context;

    public KhachHangAdapter(List<KhachHang> khachHangList, Context context) {
        this.khachHangList = khachHangList;
        this.context = context;
    }

    @NonNull
    @Override
    public KhachHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new KhachHangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KhachHangViewHolder holder, int position) {
        KhachHang khachHang = khachHangList.get(position);
        holder.text1.setText(khachHang.getTen());
        holder.text2.setText(khachHang.getSoDienThoai());
    }

    @Override
    public int getItemCount() {
        return khachHangList != null ? khachHangList.size() : 0;
    }

    public static class KhachHangViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public KhachHangViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}