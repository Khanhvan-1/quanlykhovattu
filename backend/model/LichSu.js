const mongoose = require("mongoose");

const lichSuSchema = new mongoose.Schema(
  {
    loai: {
      type: String,
      enum: ["nhap", "xuat", "system"],
      required: true,
    },

    maPhieu: { type: String, required: true },

    tenSanPham: { type: String, default: "" },

    soLuong: { type: Number, default: 0 },
    gia: { type: Number, default: 0 },
    tongTien: { type: Number, default: 0 },

    // ⭐ THÔNG TIN NGƯỜI THỰC HIỆN
    userName: { type: String, default: "Hệ thống" }, // tên hiển thị
    role: { type: String, default: "system" },       // quyền

    // ⭐ THÊM 2 TRƯỜNG QUAN TRỌNG ĐỂ LỌC LỊCH SỬ CÁ NHÂN
    maNV: { type: String, default: "SYSTEM" },        // Mã nhân viên (NV0001)
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", default: null },

    ghiChu: { type: String, default: "" },
  },
  { timestamps: true }
);

module.exports = mongoose.model("LichSu", lichSuSchema);
