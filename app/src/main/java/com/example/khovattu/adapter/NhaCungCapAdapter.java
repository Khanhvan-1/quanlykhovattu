package com.example.khovattu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.R;
import com.example.khovattu.model.NhaCungCap;

import java.util.List;

public class NhaCungCapAdapter extends RecyclerView.Adapter<NhaCungCapAdapter.NCCVH> {

    Context context;
    List<NhaCungCap> list;
    OnNCCAction callback;

    public interface OnNCCAction {
        void onEdit(NhaCungCap ncc);
        void onDelete(NhaCungCap ncc);
    }

    public NhaCungCapAdapter(Context context, List<NhaCungCap> list, OnNCCAction callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public NCCVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_ncc, parent, false);
        return new NCCVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NCCVH h, int pos) {
        NhaCungCap n = list.get(pos);

        h.tvName.setText(n.getTenNCC());
        h.tvPhone.setText("SĐT: " + n.getSoDienThoai());
        h.tvAddress.setText("Địa chỉ: " + n.getDiaChi());

        h.btnEdit.setOnClickListener(v -> callback.onEdit(n));
        h.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setTitle("Xoá nhà cung cấp")
                        .setMessage("Bạn có chắc muốn xoá " + n.getTenNCC() + "?")
                        .setPositiveButton("Xoá", (d, w) -> callback.onDelete(n))
                        .setNegativeButton("Hủy", null)
                        .show()
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NCCVH extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone, tvAddress;
        ImageButton btnEdit, btnDelete;

        public NCCVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTenNCC);
            tvPhone = itemView.findViewById(R.id.tvSdtNCC);
            tvAddress = itemView.findViewById(R.id.tvDiaChiNCC);
            btnEdit = itemView.findViewById(R.id.btnEditNCC);
            btnDelete = itemView.findViewById(R.id.btnDeleteNCC);
        }
    }
}
