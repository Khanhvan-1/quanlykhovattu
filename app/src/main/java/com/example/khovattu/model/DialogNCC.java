package com.example.khovattu.model;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.khovattu.R;

public class DialogNCC extends Dialog {

    EditText edtTen, edtSdt, edtDiaChi;
    Button btnSave, btnCancel;

    public interface NCCListener {
        void onSubmit(NhaCungCap ncc, boolean isEdit);
    }

    public DialogNCC(@NonNull Context context, NhaCungCap old, NCCListener callback) {
        super(context);
        setContentView(R.layout.dialog_ncc);

        edtTen = findViewById(R.id.edtTenNCC);
        edtSdt = findViewById(R.id.edtSdtNCC);
        edtDiaChi = findViewById(R.id.edtDiaChiNCC);
        btnSave = findViewById(R.id.btnSaveNCC);
        btnCancel = findViewById(R.id.btnCancelNCC);

        boolean isEdit = old != null;

        if (isEdit) {
            edtTen.setText(old.getTenNCC());
            edtSdt.setText(old.getSoDienThoai());
            edtDiaChi.setText(old.getDiaChi());
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            NhaCungCap n = new NhaCungCap();

            if (isEdit) n.setId(old.getId());

            n.setTenNCC(edtTen.getText().toString().trim());
            n.setSoDienThoai(edtSdt.getText().toString().trim());
            n.setDiaChi(edtDiaChi.getText().toString().trim());

            callback.onSubmit(n, isEdit);
            dismiss();
        });
    }
}
