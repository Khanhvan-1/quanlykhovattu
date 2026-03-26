package com.example.khovattu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.khovattu.R;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.NhanVien;
import com.example.khovattu.model.NhanVienResponse;
import com.example.khovattu.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NhanVienAdapter extends RecyclerView.Adapter<NhanVienAdapter.ViewHolder> {

    private final List<NhanVien> list;
    private final Context context;
    private final ApiService api;
    private final SessionManager session;

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private OnDataChangedListener listener;

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.listener = listener;
    }

    private static final int ADMIN = 0;
    private static final int NHAP = 1;
    private static final int XUAT = 2;

    public NhanVienAdapter(List<NhanVien> list, Context context,
                           ApiService api, SessionManager session) {
        this.list = list;
        this.context = context;
        this.api = api;
        this.session = session;
    }

    @Override
    public int getItemViewType(int position) {
        String role = list.get(position).getChucVu();
        if (role == null) return NHAP;
        switch (role) {
            case "admin": return ADMIN;
            case "nhap_kho": return NHAP;
            case "xuat_kho": return XUAT;
            default: return NHAP;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view;

        switch (viewType) {
            case ADMIN:
                view = inflater.inflate(R.layout.item_nhanvien_admin, parent, false);
                break;
            case XUAT:
                view = inflater.inflate(R.layout.item_nhanvien_xuat, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.item_nhanvien_nhap, parent, false);
                break;
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        NhanVien nv = list.get(position);

        h.txtTen.setText(nv.getTenNV());
        h.txtEmail.setText(nv.getEmail());
        h.txtChucVu.setText("Chức vụ: " + nv.getChucVu());
        h.txtTuoi.setText("Tuổi: " + nv.getTuoi());
        h.txtGioiTinh.setText("Giới tính: " + nv.getGioiTinh());

        switch (nv.getChucVu()) {
            case "admin": h.imgRole.setImageResource(R.drawable.ic_user_admin); break;
            case "nhap_kho": h.imgRole.setImageResource(R.drawable.ic_user_nhap); break;
            case "xuat_kho": h.imgRole.setImageResource(R.drawable.ic_user_xuat); break;
        }

        // XÓA
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("🗑 Xóa nhân viên")
                    .setMessage("Bạn chắc chắn muốn xóa " + nv.getTenNV() + "?")
                    .setPositiveButton("Xóa", (dialog, which) -> xoaNhanVien(nv.getMaNV(), position))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // SỬA
        h.btnEdit.setOnClickListener(v -> openEditDialog(nv));
    }


    // ===========================
    // ⭐ DIALOG SỬA NHÂN VIÊN
    // ===========================
    private void openEditDialog(NhanVien nv) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_them_nhanvien, null);

        EditText edtTen = view.findViewById(R.id.edtTenNV);
        Spinner spGioiTinh = view.findViewById(R.id.spnGioiTinh);
        EditText edtTuoi = view.findViewById(R.id.edtTuoi);
        Spinner spChucVu = view.findViewById(R.id.spnChucVu);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtMatKhau = view.findViewById(R.id.edtMatKhau);

        // GÁN DATA CŨ
        edtTen.setText(nv.getTenNV());
        edtEmail.setText(nv.getEmail());
        edtTuoi.setText(String.valueOf(nv.getTuoi()));

        switch (nv.getGioiTinh()) {
            case "Nữ": spGioiTinh.setSelection(1); break;
            case "Khác": spGioiTinh.setSelection(2); break;
            default: spGioiTinh.setSelection(0);
        }

        switch (nv.getChucVu()) {
            case "admin": spChucVu.setSelection(0); break;
            case "nhap_kho": spChucVu.setSelection(1); break;
            case "xuat_kho": spChucVu.setSelection(2); break;
        }

        new AlertDialog.Builder(context)
                .setTitle("✏ Sửa nhân viên")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {

                    String ten = edtTen.getText().toString().trim();
                    String email = edtEmail.getText().toString().trim();
                    String pass = edtMatKhau.getText().toString().trim();
                    String gioiTinh = spGioiTinh.getSelectedItem().toString();

                    int tuoi;
                    try { tuoi = Integer.parseInt(edtTuoi.getText().toString()); }
                    catch (Exception e) { Toast.makeText(context, "⚠ Tuổi sai!", Toast.LENGTH_SHORT).show(); return; }

                    String raw = spChucVu.getSelectedItem().toString();
                    String chucVu =
                            raw.contains("Admin") ? "admin" :
                                    raw.contains("Nhập") ? "nhap_kho" :
                                            raw.contains("Xuất") ? "xuat_kho" : "nhap_kho";

                    capNhatNhanVien(nv.getMaNV(), ten, email, tuoi, gioiTinh, chucVu, pass);

                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    // ===========================
    // ⭐ API SỬA NHÂN VIÊN
    // ===========================
    private void capNhatNhanVien(String maNV, String ten, String email, int tuoi,
                                 String gioiTinh, String chucVu, String pass) {

        NhanVien nv = new NhanVien();
        nv.setTenNV(ten);
        nv.setEmail(email);
        nv.setTuoi(tuoi);
        nv.setGioiTinh(gioiTinh);
        nv.setChucVu(chucVu);

        if (!pass.isEmpty()) nv.setPassword(pass);

        api.capNhatNhanVien(session.getToken(), maNV, nv)
                .enqueue(new Callback<NhanVienResponse>() {
                    @Override
                    public void onResponse(Call<NhanVienResponse> call, Response<NhanVienResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "✔ Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                            if (listener != null)
                                listener.onDataChanged();

                        } else {
                            Toast.makeText(context, "❌ Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<NhanVienResponse> call, Throwable t) {
                        Toast.makeText(context, "⚠ Lỗi mạng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // ===========================
    // ❌ XOÁ NHÂN VIÊN
    // ===========================
    private void xoaNhanVien(String maNV, int position) {

        api.xoaNhanVien(session.getToken(), maNV)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        if (response.isSuccessful()) {

                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());

                            Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();

                            if (listener != null)
                                listener.onDataChanged();

                        } else {
                            Toast.makeText(context, "❌ Không thể xóa!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, "⚠ Lỗi mạng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTen, txtEmail, txtChucVu, txtTuoi, txtGioiTinh;
        ImageView imgRole, btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);

            txtTen = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtChucVu = itemView.findViewById(R.id.txtChucVuNV);
            txtTuoi = itemView.findViewById(R.id.txtAge);
            txtGioiTinh = itemView.findViewById(R.id.txtGender);

            imgRole = itemView.findViewById(R.id.imgRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
