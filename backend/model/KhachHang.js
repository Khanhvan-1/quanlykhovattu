const mongoose = require("mongoose");

const KhachHangSchema = new mongoose.Schema(
  {
    ten: { type: String, required: true },
    soDienThoai: { type: String, required: true },
    diaChi: { type: String },
    email: { type: String },
    ngayTao: { type: Date, default: Date.now },
  },
  { collection: "khachhang" }
);

module.exports = mongoose.model("KhachHang", KhachHangSchema);
