package com.example.khovattu.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import com.example.khovattu.R;
import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.UserModel;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListAdapter extends BaseAdapter {

    private final Context context;
    private final List<UserModel> dsNguoiDung;
    private final ApiService apiService;
    private final String token;

    private final Runnable refreshCallback;

    public UserListAdapter(Context context, List<UserModel> dsNguoiDung, String token, Runnable refreshCallback) {
        this.context = context;
        this.dsNguoiDung = dsNguoiDung;
        this.token = token; // token đã tự có "Bearer "
        this.refreshCallback = refreshCallback;
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
    }

    @Override
    public int getCount() { return dsNguoiDung.size(); }

    @Override
    public Object getItem(int i) { return dsNguoiDung.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);

        UserModel u = dsNguoiDung.get(pos);

        TextView tvTenUser = convertView.findViewById(R.id.tvTenUser);
        TextView tvEmailUser = convertView.findViewById(R.id.tvEmailUser);
        TextView tvRoleUser = convertView.findViewById(R.id.tvRoleUser);

        Button btnSua = convertView.findViewById(R.id.btnSua);
        Button btnXoa = convertView.findViewById(R.id.btnXoa);

        tvTenUser.setText(u.getUsername());
        tvEmailUser.setText(u.getEmail());
        tvRoleUser.setText("Quyền: " + u.getRole());

        // ⭐ SỬA USER
        btnSua.setOnClickListener(v -> showEditDialog(u));

        // ⭐ XÓA USER
        btnXoa.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa '" + u.getUsername() + "'?")
                .setPositiveButton("Xóa", (d, w) -> xoaUser(u))
                .setNegativeButton("Hủy", null)
                .show());

        return convertView;
    }

    private void xoaUser(UserModel u) {

        apiService.xoaUser(token, u.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> res) {
                        if (res.isSuccessful()) {
                            Toast.makeText(context, "Đã xóa " + u.getUsername(), Toast.LENGTH_SHORT).show();
                            refreshCallback.run();
                        } else {
                            Toast.makeText(context, "Lỗi xóa người dùng!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEditDialog(UserModel u) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);

        EditText edtName = view.findViewById(R.id.edtEditName);
        EditText edtEmail = view.findViewById(R.id.edtEditEmail);
        Spinner spRole = view.findViewById(R.id.spEditRole);

        edtName.setText(u.getUsername());
        edtEmail.setText(u.getEmail());

        String[] roles = {"admin", "nhap_kho", "xuat_kho"};
        ArrayAdapter<String> ad = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, roles);
        spRole.setAdapter(ad);

        for (int i = 0; i < roles.length; i++)
            if (roles[i].equals(u.getRole()))
                spRole.setSelection(i);

        new AlertDialog.Builder(context)
                .setTitle("Sửa: " + u.getUsername())
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {

                    Map<String, Object> body = new HashMap<>();
                    body.put("username", edtName.getText().toString().trim());
                    body.put("email", edtEmail.getText().toString().trim());
                    body.put("role", spRole.getSelectedItem().toString());

                    apiService.capNhatUser(token, u.getId(), body)
                            .enqueue(new Callback<Map<String, Object>>() {
                                @Override
                                public void onResponse(Call<Map<String, Object>> call,
                                                       Response<Map<String, Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                        refreshCallback.run();
                                    } else {
                                        Toast.makeText(context, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

}
