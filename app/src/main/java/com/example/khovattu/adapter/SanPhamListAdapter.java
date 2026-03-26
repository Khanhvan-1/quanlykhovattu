package com.example.khovattu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.cardview.widget.CardView;

import com.example.khovattu.R;
import com.example.khovattu.ThemSanPhamActivity;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.SanPham;
import com.example.khovattu.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.List;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SanPhamListAdapter extends BaseAdapter {

    private final Context context;
    private final List<SanPham> sanPhamList;
    private final LayoutInflater inflater;

    private final SessionManager session;
    private final ApiService api;
    private final String role, token;

    public SanPhamListAdapter(Context ctx, List<SanPham> list) {
        this.context = ctx;
        this.sanPhamList = list;
        this.inflater = LayoutInflater.from(ctx);

        session = new SessionManager(ctx);
        api = ApiClient.getClient(ctx).create(ApiService.class);

        role = session.getRole();     // admin | nhap_kho | xuat_kho
        token = session.getToken();
    }

    @Override
    public int getCount() {
        return sanPhamList != null ? sanPhamList.size() : 0;
    }

    @Override
    public Object getItem(int pos) {
        return sanPhamList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {

        ViewHolder h;

        if (view == null) {
            view = inflater.inflate(R.layout.item_sanpham, parent, false);
            h = new ViewHolder();

            h.card = view.findViewById(R.id.cardSanPham);
            h.tvTen = view.findViewById(R.id.tvTenSP);
            h.tvMa = view.findViewById(R.id.tvMaSP);
            h.tvGia = view.findViewById(R.id.tvGiaSP);
            h.tvTon = view.findViewById(R.id.tvSoLuong);
            h.imgWarning = view.findViewById(R.id.imgWarning);
            h.btnEdit = view.findViewById(R.id.btnEdit);
            h.btnDelete = view.findViewById(R.id.btnDelete);

            view.setTag(h);
        } else {
            h = (ViewHolder) view.getTag();
        }

        SanPham sp = sanPhamList.get(pos);

        // ======== Bind data ========
        DecimalFormat fmt = new DecimalFormat("###,###");
        h.tvTen.setText(sp.getTenHang());
        h.tvMa.setText("Mã: " + sp.getMaHang());
        h.tvGia.setText("Giá nhập: " + fmt.format(sp.getGiaNhap()) + " ₫");

        int ton = sp.getTonKho() > 0 ? sp.getTonKho() : sp.getSoLuong();
        h.tvTon.setText("Tồn kho: " + ton);

        // ================= UI CẢNH BÁO ==================
        if (ton < 10) {
            h.tvTon.setTextColor(context.getColor(R.color.red_error));
            h.imgWarning.setVisibility(View.VISIBLE);

            h.card.setCardBackgroundColor(context.getColor(R.color.light_warning_bg));
            h.card.setForeground(context.getDrawable(R.drawable.border_glow));

            Animation pulse = AnimationUtils.loadAnimation(context, R.anim.glow_pulse);
            h.card.startAnimation(pulse);

        } else {
            h.tvTon.setTextColor(context.getColor(R.color.blue_700));
            h.imgWarning.setVisibility(View.GONE);

            h.card.setCardBackgroundColor(context.getColor(android.R.color.white));
            h.card.setForeground(null);
            h.card.clearAnimation();
        }


        // =================================================================
        //               🔧 PH N  QUY N  THEO VAI TRÒ
        // =================================================================

        // ✏ Sửa → chỉ admin & nhập kho
        if (role.equals("xuat_kho")) {
            h.btnEdit.setVisibility(View.GONE);    // xuất kho không được sửa
        } else {
            h.btnEdit.setVisibility(View.VISIBLE);
        }

        h.btnEdit.setOnClickListener(v -> {
            if (role.equals("xuat_kho")) {
                Toast.makeText(context, "Bạn không có quyền sửa sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(context, ThemSanPhamActivity.class);
            i.putExtra("SAN_PHAM_DE_SUA", sp);
            context.startActivity(i);
        });


        // 🗑 Xoá → chỉ admin
        if (!role.equals("admin")) {
            h.btnDelete.setVisibility(View.GONE);
        } else {
            h.btnDelete.setVisibility(View.VISIBLE);
        }

        h.btnDelete.setOnClickListener(v -> {

            if (!role.equals("admin")) {
                Toast.makeText(context, "Chỉ admin được xoá sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Xoá sản phẩm")
                    .setMessage("Bạn chắc chắn muốn xoá \"" + sp.getTenHang() + "\"?")
                    .setPositiveButton("Xoá", (d, w) -> {

                        api.xoaSanPham(token, sp.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> r) {
                                if (r.isSuccessful()) {
                                    sanPhamList.remove(pos);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "🗑️ Đã xoá thành công!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "❌ Lỗi xoá: " + r.code(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(context, "🚫 " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return view;
    }

    static class ViewHolder {
        CardView card;
        TextView tvTen, tvMa, tvGia, tvTon;
        ImageView imgWarning;
        ImageButton btnEdit, btnDelete;
    }
}
