package com.example.khovattu.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.example.khovattu.R;
import com.example.khovattu.model.NhaCungCap;

import java.util.ArrayList;
import java.util.List;

public class NCCBottomAdapter extends BaseAdapter {

    private Context ctx;
    private List<NhaCungCap> data;
    private List<NhaCungCap> dataFull;

    public NCCBottomAdapter(Context ctx, List<NhaCungCap> list) {
        this.ctx = ctx;
        this.data = list;
        this.dataFull = new ArrayList<>(list);
    }

    @Override
    public int getCount() { return data.size(); }

    @Override
    public Object getItem(int i) { return data.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View v, ViewGroup parent) {
        if (v == null)
            v = LayoutInflater.from(ctx).inflate(R.layout.item_ncc_bottom, parent, false);

        NhaCungCap n = data.get(i);

        ((TextView) v.findViewById(R.id.tvTenNCC)).setText(n.getTenNCC());
        ((TextView) v.findViewById(R.id.tvSdtNCC)).setText("SĐT: " + n.getSoDienThoai());
        ((TextView) v.findViewById(R.id.tvDiaChiNCC)).setText("Địa chỉ: " + n.getDiaChi());

        return v;
    }

    // FILTER (SEARCH)
    public void filter(String text) {
        text = text.toLowerCase();
        data.clear();

        if (text.isEmpty()) {
            data.addAll(dataFull);
        } else {
            for (NhaCungCap n : dataFull) {
                if (n.getTenNCC().toLowerCase().contains(text)
                        || n.getSoDienThoai().toLowerCase().contains(text)
                        || n.getDiaChi().toLowerCase().contains(text)) {

                    data.add(n);
                }
            }
        }

        notifyDataSetChanged();
    }
}
