package com.example.khovattu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;

import android.graphics.Color;
import androidx.core.content.ContextCompat;
import com.example.khovattu.R;
import com.example.khovattu.model.LichSu;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichSuListAdapter extends BaseAdapter {

    private final List<LichSu> lichSuList;
    private final Context context;
    private final LayoutInflater inflater;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public LichSuListAdapter(Context context, List<LichSu> lichSuList) {
        this.context = context;
        this.lichSuList = lichSuList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lichSuList != null ? lichSuList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return lichSuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_lich_su, parent, false);
            holder = new ViewHolder();
            holder.imgLoaiPhieu = convertView.findViewById(R.id.imgLoaiPhieu);
            holder.tvLoaiPhieu = convertView.findViewById(R.id.tvLoaiPhieu);
            holder.tvTenSP = convertView.findViewById(R.id.tvTenSP);
            holder.tvThoiGian = convertView.findViewById(R.id.tvThoiGian);
            holder.tvSoLuong = convertView.findViewById(R.id.tvSoLuong);
            holder.tvNguoiThucHien = convertView.findViewById(R.id.tvNguoiThucHien);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LichSu lichSu = lichSuList.get(position);
        if (lichSu == null) return convertView;

        MaterialCardView card = convertView.findViewById(R.id.cardLichSu);

        String roleDisplay = lichSu.getRole();
        if (roleDisplay == null) roleDisplay = "system";

        int borderColor;

        switch (roleDisplay) {
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

        card.setStrokeWidth(6);
        card.setStrokeColor(borderColor);

        card.setRadius(12);
        card.setCardBackgroundColor(Color.WHITE);
        card.setContentPadding(6, 6, 6, 6);
        card.setStrokeWidth(6);
        card.setStrokeColor(borderColor);
        if (lichSu == null) return convertView;

        String loai = lichSu.getLoai() != null ? lichSu.getLoai().toLowerCase() : "";

        holder.tvTenSP.setText(lichSu.getTenSanPham());

        String nguoi = lichSu.getUserName();
        if (nguoi == null || nguoi.isEmpty()) nguoi = "Hệ thống";

        String role = lichSu.getRole();
        String chucVu = "";

        if (role != null) {
            switch (role) {
                case "admin": chucVu = "Admin"; break;
                case "nhap_kho": chucVu = "Nhập kho"; break;
                case "xuat_kho": chucVu = "Xuất kho"; break;
                default: chucVu = ""; break; // system → không hiển thị
            }
        }

        if (chucVu.isEmpty()) {
            holder.tvNguoiThucHien.setText("👤 " + nguoi);
        } else {
            holder.tvNguoiThucHien.setText("👤 " + nguoi + " – " + chucVu);
        }

        String tg = lichSu.getCreatedAt();

        if (tg != null) {
            try {
                String clean = tg.replace("T", " ")
                        .replace("Z", "")
                        .substring(0, 19);
                SimpleDateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                isoParser.setLenient(false);
                Date date = isoParser.parse(clean);

                holder.tvThoiGian.setText(sdf.format(date));
            }
            catch (Exception e) {
                holder.tvThoiGian.setText(tg);
            }
        }
        else {
            holder.tvThoiGian.setText("Không rõ");
        }

        if (loai.equals("nhap")) {
            holder.tvLoaiPhieu.setText("Nhập kho");
            holder.tvSoLuong.setText("+" + lichSu.getSoLuong());
            holder.tvSoLuong.setTextColor(ContextCompat.getColor(context, R.color.green_success));
            holder.imgLoaiPhieu.setImageResource(R.drawable.ic_nhapkho);

        } else if (loai.equals("xuat")) {
            holder.tvLoaiPhieu.setText("Xuất kho");
            holder.tvSoLuong.setText("-" + lichSu.getSoLuong());
            holder.tvSoLuong.setTextColor(ContextCompat.getColor(context, R.color.red_error));
            holder.imgLoaiPhieu.setImageResource(R.drawable.ic_xuatkho);

        } else {
            holder.tvLoaiPhieu.setText("Khác");
            holder.tvSoLuong.setText("" + lichSu.getSoLuong());
            holder.tvSoLuong.setTextColor(ContextCompat.getColor(context, R.color.blue_700));
            holder.imgLoaiPhieu.setImageResource(R.drawable.ic_report);
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView imgLoaiPhieu;
        TextView tvLoaiPhieu, tvTenSP, tvThoiGian, tvSoLuong, tvNguoiThucHien;
    }
}
