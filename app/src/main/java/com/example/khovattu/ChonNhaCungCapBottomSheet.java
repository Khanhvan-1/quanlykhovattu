package com.example.khovattu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.khovattu.adapter.NCCBottomAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.model.NhaCungCap;
import com.example.khovattu.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChonNhaCungCapBottomSheet extends BottomSheetDialogFragment {

    public interface OnNCCSelected {
        void onSelected(NhaCungCap ncc);
    }

    private OnNCCSelected callback;

    public ChonNhaCungCapBottomSheet(OnNCCSelected callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottomsheet_ncc, container, false);

        EditText edtSearch = view.findViewById(R.id.edtSearchNCC);
        ListView listView = view.findViewById(R.id.listNCCBottom);
        ImageView btnClose = view.findViewById(R.id.btnClose);

        SessionManager sm = new SessionManager(getContext());
        String token = sm.getToken();
        ApiService api = ApiClient.getClient(getContext()).create(ApiService.class);

        api.layDanhSachNCC(token).enqueue(new Callback<List<NhaCungCap>>() {

            @Override
            public void onResponse(Call<List<NhaCungCap>> call, Response<List<NhaCungCap>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<NhaCungCap> ds = response.body();

                NCCBottomAdapter adapter = new NCCBottomAdapter(getContext(), ds);
                listView.setAdapter(adapter);

                // Làm search realtime
                edtSearch.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
                    @Override public void afterTextChanged(Editable s) {}

                    @Override
                    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                        adapter.filter(s.toString());
                    }
                });

                // Bấm chọn NCC
                listView.setOnItemClickListener((parent, view, pos, id) -> {
                    NhaCungCap selected = (NhaCungCap) adapter.getItem(pos);
                    callback.onSelected(selected);
                    dismiss();
                });
            }

            @Override
            public void onFailure(Call<List<NhaCungCap>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải danh sách NCC!", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }
}
