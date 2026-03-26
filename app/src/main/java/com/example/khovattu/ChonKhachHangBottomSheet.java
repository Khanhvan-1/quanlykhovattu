package com.example.khovattu;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.KhachHang;
import com.example.khovattu.adapter.KhachHangBottomAdapter;
import com.example.khovattu.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChonKhachHangBottomSheet extends BottomSheetDialogFragment {

    public interface OnKhachHangSelected {
        void onSelected(KhachHang kh);
    }

    private OnKhachHangSelected callback;

    public ChonKhachHangBottomSheet(OnKhachHangSelected callback) {
        this.callback = callback;
    }

    private ArrayList<KhachHang> dsFull = new ArrayList<>();
    private KhachHangBottomAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottomsheet_khachhang, null);
        dialog.setContentView(view);

        EditText edtSearch = view.findViewById(R.id.edtSearchKH);
        ListView listView = view.findViewById(R.id.listKHBtm);
        ImageView btnClose = view.findViewById(R.id.btnCloseKH);

        SessionManager sm = new SessionManager(getContext());
        String token = sm.getToken();
        ApiService api = ApiClient.getClient(getContext()).create(ApiService.class);

        // ======= LOAD DỮ LIỆU KHÁCH HÀNG =======
        api.layDanhSachKhachHang(token).enqueue(new Callback<List<KhachHang>>() {
            @Override
            public void onResponse(Call<List<KhachHang>> call, Response<List<KhachHang>> res) {
                if (!res.isSuccessful() || res.body() == null) return;

                dsFull.clear();
                dsFull.addAll(res.body());

                adapter = new KhachHangBottomAdapter(getContext(), dsFull);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((parent, v, pos, id) -> {
                    callback.onSelected(dsFull.get(pos));
                    dismiss();
                });
            }

            @Override
            public void onFailure(Call<List<KhachHang>> call, Throwable t) {}
        });

        // 🔍 ====== TÌM KIẾM REALTIME ======
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString().toLowerCase();

                ArrayList<KhachHang> filter = new ArrayList<>();

                for (KhachHang k : dsFull) {
                    if (k.getTen().toLowerCase().contains(key)
                            || k.getSoDienThoai().contains(key)
                            || k.getDiaChi().toLowerCase().contains(key)) {

                        filter.add(k);
                    }
                }

                adapter = new KhachHangBottomAdapter(getContext(), filter);
                listView.setAdapter(adapter);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        btnClose.setOnClickListener(v -> dismiss());

        return dialog;
    }
}
