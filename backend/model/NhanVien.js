const mongoose = require("mongoose");

const NhanVienSchema = new mongoose.Schema(
  {
    maNV: {
      type: String,
      required: true,
      unique: true,
      trim: true,
    },

    tenNV: {
      type: String,
      required: true,
      trim: true
    },

    gioiTinh: {
      type: String,
      enum: ["Nam", "Nữ", "Khác"],
      required: true,
      default: "Nam",
    },

    tuoi: {
      type: Number,
      min: 18,
      required: true,
    },

    chucVu: {
      type: String,
      enum: ["admin", "nhap_kho", "xuat_kho"],
      required: true,
    },

    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },

    password: {
      type: String,
      required: true,
      minlength: 6,
    },
  },
  { timestamps: true }
);
module.exports = mongoose.model("NhanVien", NhanVienSchema);
