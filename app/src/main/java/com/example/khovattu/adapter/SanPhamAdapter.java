package com.example.khovattu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.R;
import com.example.khovattu.ThemSanPhamActivity;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.SanPhamViewHolder> {

    private final List<SanPham> sanPhamList;
    private final Context context;
    private final ApiService apiService;
    private final SessionManager sessionManager;

    private final String role;
    private final String token;

    public SanPhamAdapter(List<SanPham> sanPhamList, Context context) {
        this.sanPhamList = sanPhamList;
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getClient(context).create(ApiService.class);

        this.role = sessionManager.getRole();   // admin | nhap_kho | xuat_kho
        this.token = sessionManager.getToken();
    }

    @NonNull
    @Override
    public SanPhamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sanpham, parent, false);
        return new SanPhamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SanPhamViewHolder holder, int position) {

        SanPham sp = sanPhamList.get(position);
        if (sp == null) return;

        DecimalFormat fmt = new DecimalFormat("###,###,###");

        holder.tvTenSP.setText(sp.getTenHang());
        holder.tvMaSP.setText("Mã: " + sp.getMaHang());
        holder.tvGiaSP.setText("Giá nhập: " + fmt.format(sp.getGiaNhap()) + " ₫");

        int ton = sp.getTonKho() > 0 ? sp.getTonKho() : sp.getSoLuong();

        // ========= Cảnh báo tồn kho =========
        if (ton < 10) {
            holder.tvSoLuong.setText("Tồn kho thấp: " + ton);
            holder.tvSoLuong.setTextColor(context.getColor(R.color.red_error));
            holder.imgWarning.setVisibility(View.VISIBLE);

            holder.cardView.setCardBackgroundColor(context.getColor(R.color.light_warning_bg));
            holder.cardView.setForeground(context.getDrawable(R.drawable.border_glow));
            holder.cardView.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.glow_pulse)
            );
        } else {
            holder.tvSoLuong.setText("Tồn kho: " + ton);
            holder.tvSoLuong.setTextColor(context.getColor(R.color.blue_700));
            holder.imgWarning.setVisibility(View.GONE);

            holder.cardView.setCardBackgroundColor(context.getColor(android.R.color.white));
            holder.cardView.setForeground(null);
            holder.cardView.clearAnimation();
        }


        // ========================================================
        //  🔧 PHÂN QUYỀN CHỨC NĂNG
        // ========================================================

        // 1) CLICK SỬA — chỉ admin & nhập kho
        holder.itemView.setOnClickListener(v -> {
            if (role.equals("xuat_kho")) {
                Toast.makeText(context, "Bạn chỉ có quyền xem sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, ThemSanPhamActivity.class);
            intent.putExtra("SAN_PHAM_DE_SUA", sp);
            context.startActivity(intent);
        });

        // 2) LONG CLICK XOÁ — chỉ admin
        holder.itemView.setOnLongClickListener(v -> {

            if (!role.equals("admin")) {
                Toast.makeText(context,
                        "Chỉ admin được xoá sản phẩm!",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn muốn xoá '" + sp.getTenHang() + "'?")
                    .setPositiveButton("Xóa", (d, w) -> xoaSanPham(sp, holder.getAdapterPosition()))
                    .setNegativeButton("Hủy", null)
                    .show();

            return true;
        });

    }

    // ========== API XOÁ SẢN PHẨM ==========
    private void xoaSanPham(SanPham sp, int pos) {

        apiService.xoaSanPham(token, sp.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> resp) {

                if (resp.isSuccessful()) {
                    sanPhamList.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(context, "🗑 Đã xoá sản phẩm!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,
                            "❌ Xoá thất bại (" + resp.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context,
                        "⚠ Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sanPhamList != null ? sanPhamList.size() : 0;
    }

    public static class SanPhamViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham, imgWarning;
        TextView tvTenSP, tvMaSP, tvGiaSP, tvSoLuong;
        CardView cardView;

        public SanPhamViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView;

            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            imgWarning = itemView.findViewById(R.id.imgWarning);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvMaSP = itemView.findViewById(R.id.tvMaSP);
            tvGiaSP = itemView.findViewById(R.id.tvGiaSP);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
        }
    }
}
