package com.example.khovattu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.khovattu.R;
import com.example.khovattu.model.KhachHang;

import java.util.ArrayList;

public class KhachHangBottomAdapter extends BaseAdapter {

    Context context;
    ArrayList<KhachHang> list;

    public KhachHangBottomAdapter(Context context, ArrayList<KhachHang> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int position) { return list.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_khachhang_bottom, parent, false);
        }

        KhachHang kh = list.get(pos);

        TextView tvTen = convertView.findViewById(R.id.tvTen);
        TextView tvSdt = convertView.findViewById(R.id.tvSdt);
        TextView tvDiaChi = convertView.findViewById(R.id.tvDiaChi);

        tvTen.setText(kh.getTen());
        tvSdt.setText("📞 " + kh.getSoDienThoai());
        tvDiaChi.setText("📍 " + kh.getDiaChi());

        return convertView;
    }
}
