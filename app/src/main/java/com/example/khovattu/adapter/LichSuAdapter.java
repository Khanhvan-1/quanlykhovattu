package com.example.khovattu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.R;
import com.example.khovattu.model.LichSu;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.LichSuViewHolder> {

    private final List<LichSu> lichSuList;
    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public LichSuAdapter(List<LichSu> lichSuList, Context context) {
        this.lichSuList = lichSuList;
        this.context = context;
    }

    @NonNull
    @Override
    public LichSuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_su, parent, false);
        return new LichSuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LichSuViewHolder holder, int position) {
        LichSu lichSu = lichSuList.get(position);
        if (lichSu == null) return;

        // ================= ROLE BORDER COLOR =================
        String role = lichSu.getRole();
        if (role == null) role = "system";

        int borderColor;

        switch (role) {
            case "admin":
                borderColor = ContextCompat.getColor(context, R.color.role_admin);
                break;
            case "nhap_kho":
                borderColor = ContextCompat.getColor(context, R.color.role_nhap);
                break;
            case "xuat_kho":
                borderColor = ContextCompat.getColor(context, R.color.role_xuat);
                break;
            default:
                borderColor = ContextCompat.getColor(context, R.color.role_system);
                break;
        }

        holder.cardView.setStrokeWidth(6);
        holder.cardView.setStrokeColor(borderColor);
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        holder.cardView.setRadius(12);


        // ================= LOẠI PHIẾU =================
        String loai = lichSu.getLoai() != null ? lichSu.getLoai().toLowerCase() : "";

        holder.tvTenSP.setText(lichSu.getTenSanPham());

        if (loai.equals("nhap")) {
            holder.tvLoaiPhieu.setText("Nhập kho");
            holder.tvSoLuong.setText("+" + lichSu.getSoLuong());
            holder.tvSoLuong.setTextColor(ContextCompat.getColor(context, R.color.green_success));
            holder.imgLoaiPhieu.setImageResource(R.drawable.ic_nhapkho);
        }
        else if (loai.equals("xuat")) {
            holder.tvLoaiPhieu.setText("Xuất kho");
            holder.tvSoLuong.setText("-" + lichSu.getSoLuong());
            holder.tvSoLuong.setTextColor(ContextCompat.getColor(context, R.color.red_error));
            holder.imgLoaiPhieu.setImageResource(R.drawable.ic_xuatkho);
        }
        else {
            holder.tvLoaiPhieu.setText("Khác");
            holder.tvSoLuong.setText(String.valueOf(lichSu.getSoLuong()));
            holder.tvSoLuong.setTextColor(ContextCompat.getColor(context, R.color.blue_700));
            holder.imgLoaiPhieu.setImageResource(R.drawable.ic_report);
        }

        // ================= NGƯỜI THỰC HIỆN + CHỨC VỤ =================
        String name = lichSu.getUserName();
        if (name == null || name.isEmpty()) name = "Hệ thống";

        String chucVu = "";
        switch (role) {
            case "admin": chucVu = "Admin"; break;
            case "nhap_kho": chucVu = "Nhập kho"; break;
            case "xuat_kho": chucVu = "Xuất kho"; break;
        }

        if (chucVu.isEmpty()) {
            holder.tvNguoiThucHien.setText("👤 " + name);
        } else {
            holder.tvNguoiThucHien.setText("👤 " + name + " – " + chucVu);
        }


        // ================= THỜI GIAN =================
        String tg = lichSu.getCreatedAt();
        if (tg != null && tg.length() > 10) {
            try {
                String formatted = sdf.format(java.sql.Timestamp.valueOf(
                        tg.replace("T", " ").replace("Z", "")));
                holder.tvThoiGian.setText(formatted);
            } catch (Exception e) {
                holder.tvThoiGian.setText(tg);
            }
        } else {
            holder.tvThoiGian.setText("Không rõ thời gian");
        }
    }

    @Override
    public int getItemCount() {
        return lichSuList != null ? lichSuList.size() : 0;
    }

    // ================= VIEW HOLDER =================
    public static class LichSuViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imgLoaiPhieu;
        TextView tvLoaiPhieu, tvTenSP, tvSoLuong, tvNguoiThucHien, tvThoiGian;

        public LichSuViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardLichSu);
            imgLoaiPhieu = itemView.findViewById(R.id.imgLoaiPhieu);
            tvLoaiPhieu = itemView.findViewById(R.id.tvLoaiPhieu);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvNguoiThucHien = itemView.findViewById(R.id.tvNguoiThucHien);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
        }
    }
}
